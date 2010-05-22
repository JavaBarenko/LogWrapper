package barenko.util;

/*
 * Dispara quando uma interface não pode ser implementada em uma determinada classe.
 */
public class IllegalInterfaceImplementationException extends Exception {
    public IllegalInterfaceImplementationException(final Class<?> instanceClass, final Class<?> interfaceMark) {
	super(String.format("The %s class can not implements %s interface because the class not assign the interface contract.", instanceClass.getSuperclass().getName(), interfaceMark.getName()));
    }
}
