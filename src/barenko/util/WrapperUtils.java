package barenko.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;


public class WrapperUtils {
    private static final String FULLDATE_FORMAT_JAVA = "yyyyMMddHHmmss";

    /**
     * Efetua um wrapper de log na instancia especificada.<br>
     * � obrigat�rio que a inst�ncia possua ao menos uma interface e ser� essa interface que ser� retornada pelo m�todo.
     * Caso haja mais de uma interface, o retorno pode ser qualquer uma delas.<br>
     * O log e n�vel de log que ser�o utilizados tamb�m devem ser especificados aqui. Esse log n�o tem nenhum v�nculo
     * com qualquer outro log que a inst�ncia original possa estar utilizando.<br>
     * <br>
     * O efeito imediato da inst�ncia retornada por esse m�todo � logar (atrav�s do log e n�vel especificados) todas as
     * chamadas de m�todo(incluindo argumentos) e seus respectivos retornos.<br>
     * <br>
     * O log segue o seguinte formato:<br>
     * - Para chamada: [nome da thread] pacote.classe.metodo(arg1, arg2.. argN)<br>
     * - Para retorno: [nome da thread] return: [valor retornado]<br>
     * - Para throw: [nome da thread] throw a ExceptionClass: Mensagem\nTrace<br>
     * 
     * @param instanceByInterface
     *            Uma inst�ncia interfaceada (a refer�ncia de retorno DEVE ser uma das interfaces implementadas por essa
     *            inst�ncia)
     * @param logger
     *            O {@link Logger} que ser� utilizado para o trace dos m�todos
     * @param logLevel
     *            O n�vel que ser� logado todos os logs de trace, os n�veis est�o dispon�veis em {@link Logger}
     * @return A inst�ncia especificada encapsulada em um proxy de log
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> T enableTraceByInterface(final T instanceByInterface, final Logger logger, final Level logLevel) {
	final ClassLoader classLoader = instanceByInterface.getClass().getClassLoader();
	final Class[] interfaces = instanceByInterface.getClass().getInterfaces();
	final LoggerMethodInterceptor handler = new LoggerMethodInterceptor(instanceByInterface, logger, logLevel);
	return (T) Proxy.newProxyInstance(classLoader, interfaces, handler);
    }

    /**
     * Efetua um wrapper de log na instancia especificada.<br>
     * O log e n�vel de log que ser�o utilizados tamb�m devem ser especificados aqui. Esse log n�o tem nenhum v�nculo
     * com qualquer outro log que a inst�ncia original possa estar utilizando.<br>
     * <br>
     * O efeito imediato da inst�ncia retornada por esse m�todo � logar (atrav�s do log e n�vel especificados) todas as
     * chamadas de m�todo(incluindo argumentos) e seus respectivos retornos.<br>
     * <br>
     * O log segue o seguinte formato:<br>
     * - Para chamada: [nome da thread] pacote.classe.metodo(arg1, arg2.. argN)<br>
     * - Para retorno: [nome da thread] return: [valor retornado]<br>
     * - Para throw: [nome da thread] throw a ExceptionClass: Mensagem\nTrace<br>
     * 
     * @param instance
     *            Uma inst�ncia
     * @param logger
     *            O {@link Logger} que ser� utilizado para o trace dos m�todos
     * @param logLevel
     *            O n�vel que ser� logado todos os logs de trace, os n�veis est�o dispon�veis em {@link Logger}
     * @return A inst�ncia especificada com bytecode alterado
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> T enableTrace(final T instance, final Logger logger, final Level logLevel) {
	final MethodInterceptor methodInterceptor = new LoggerMethodInterceptor(null, logger, logLevel);
	return (T) Enhancer.create(instance.getClass(), methodInterceptor);
    }

    /**
     * Adiciona uma interface � inst�ncia especificada.<br>
     * Caso a instancia n�o possua os m�todos especificados pela interface, ela n�o ser� anexada � inst�ncia.
     * 
     * @param instance
     *            Uma inst�ncia qualquer
     * @param interfaceMark
     *            Uma interface marcadora
     * @return A inst�ncia especificada com a nova interface implementada OU a mesma instancia sem altera��o (caso a
     *         inst�ncia n�o tenha os m�todos necess�rios para implementar a interface)
     * @throws IllegalInterfaceImplementationException
     *             Se a instancia n�o puder implementar a interface
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> T addInterface(final T instance, final Class interfaceMark) throws IllegalInterfaceImplementationException {
	if (!interfaceMark.isInterface()) return instance;
	final MethodInterceptor methodInterceptor = new MethodInterceptor() {
	    public Object intercept(final Object instance, @SuppressWarnings("unused") final Method method, final Object[] args, final MethodProxy proxy) throws Throwable {
		return proxy.invokeSuper(instance, args);
	    }
	};

	Object answer = null;
	try {
	    answer = Enhancer.create(instance.getClass(), new Class[] { interfaceMark }, methodInterceptor);
	} catch (final Throwable t) {
	    throw new IllegalInterfaceImplementationException(instance.getClass(), interfaceMark);
	}
	return (T) answer;
    }


    private static class LoggerMethodInterceptor implements MethodInterceptor, InvocationHandler {
	private final Object instance;
	private final Logger logger;
	private final Level logLevel;

	private LoggerMethodInterceptor(final Object instance, final Logger logger, final Level logLevel) {
	    this.instance = instance;
	    this.logger = logger;
	    this.logLevel = logLevel;
	}

	public Object invoke(@SuppressWarnings("unused") final Object proxy, final Method method, final Object[] args) throws Throwable {
	    logMethodCall(method, args);
	    Object answer = null;
	    try {
		method.setAccessible(true);
		answer = method.invoke(this.instance, args);
	    } catch (final Throwable t) {
		logAndThrow(t);
	    }
	    logMethodReturn(answer);
	    return answer;
	}

	public Object intercept(final Object instance, final Method method, final Object[] args, final MethodProxy proxy) throws Throwable {
	    logMethodCall(method, args);
	    Object answer = null;
	    try {
		answer = proxy.invokeSuper(instance, args);
	    } catch (final Throwable t) {
		logAndThrow(t);
	    }
	    logMethodReturn(answer);
	    return answer;
	}

	private void logMethodCall(final Method method, final Object[] args) {
	    final String msg = formatMessage(method, args);
	    this.logger.log(this.logLevel, msg);
	}

	private void logMethodReturn(final Object answer) {
	    this.logger.log(this.logLevel, formatReturnMessage(answer));
	}

	private void logAndThrow(final Throwable t) throws Throwable {
	    final Throwable realThrowable = t.getCause() == null ? t : t.getCause();
	    this.logger.log(this.logLevel, formatThrowableMessage(realThrowable, t.getCause() != null));
	    throw realThrowable;
	}

	private String formatThrowableMessage(final Throwable throwable, final boolean skip) {
	    final String message = throwable.getMessage();
	    final String stackTrace = stackTraceToString(throwable.getStackTrace(), skip);
	    return String.format("[%s] throw a %s: %s\n%s", Thread.currentThread(), throwable.getClass().getName(), message, stackTrace);
	}

	private String formatReturnMessage(final Object answer) {
	    String value = null;

	    if (answer == null) value = "null";
	    else if (answer.getClass().isArray()) value = "[" + arraysToString((Object[]) answer) + "]";
	    else value = answer.toString();

	    return String.format("[%s] return: [%s]", Thread.currentThread(), value);
	}

	private String formatMessage(final Method method, final Object[] args) {
	    final String arguments = formatArguments(args);
	    return String.format("[%s] %s.%s(%s)", Thread.currentThread(), method.getDeclaringClass().getName(), method.getName(), arguments);
	}

	private String formatArguments(final Object[] args) {
	    String arguments = arraysToString(args);
	    if ("null".equals(arguments)) arguments = "";
	    return arguments;
	}


	private String arraysToString(final Object[] args) {
	    if (args == null || args.length == 0) return "";

	    final DateFormat dt = new SimpleDateFormat(FULLDATE_FORMAT_JAVA);
	    final StringBuilder sb = new StringBuilder();
	    for (final Object o : args) {
		if (o == null) sb.append("null");
		else if (o.getClass().isArray()) sb.append("[").append(arraysToString((Object[]) o)).append("]");
		else if (o instanceof CharSequence) sb.append("\"").append(o).append("\"");
		else if (o instanceof Date) sb.append(dt.format(o));
		else sb.append(o.toString());

		sb.append(", ");
	    }
	    sb.setLength(sb.length() - 2);
	    return sb.toString();
	}

	private String stackTraceToString(final StackTraceElement[] stack, final boolean skip) {
	    final int maxToSkip = skip ? 6 : Integer.MIN_VALUE;
	    final int minToSkip = skip ? 1 : Integer.MAX_VALUE;

	    final StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < stack.length; i++) {
		if (i < minToSkip || i > maxToSkip) sb.append("        ").append(stack[i].toString()).append("\n");
	    }
	    sb.setLength(sb.length() - 1);
	    return sb.toString();
	}
    }

}
