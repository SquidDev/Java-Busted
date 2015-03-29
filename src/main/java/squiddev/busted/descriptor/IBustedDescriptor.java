package squiddev.busted.descriptor;

import org.luaj.vm2.Varargs;

/**
 * A context specific function to run
 */
public interface IBustedDescriptor {
	void invoke(BustedContext context, Varargs args);
}
