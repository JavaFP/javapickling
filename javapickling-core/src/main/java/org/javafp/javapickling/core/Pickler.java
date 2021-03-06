package org.javafp.javapickling.core;

/**
 * Interface for Pickler classes, which implement both the pickling and unpickling of a single type.
 * <p>
 * This is the fundamental interface in the framework,
 * which is implemented by every class which provides pickling of a specific type.
 * <p>
 * The pickle() and unpickle() methods should be complements of each other,
 * in that calling unpickle() on the result of calling pickle() should yield the original value.
 * @param <T> the type of object to be pickled.
 * @param <PF> the pickle format.
 */
public interface Pickler<T, PF> {
    /**
     * Pickle a value into the pickle target.
     * <p>
     * Whether the target argument is used depends on the format PF.
     * <p>
     * For some formats, such as JsonNode, the pickled value can be created without modifying an existing value.
     * For these cases the target argument is never used and is generally null.
     * The pickled value is then returned and used by the caller.
     * <p>
     * Other formats, such as ByteIO and XmlNode, require writing to an existing value.
     * In these cases the target argument is required.
     * The returned value generally isn't used.
     * @param t the value to be pickled.
     * @param target the pickle target. May be null if the pickled result can be encapsulated in the return value.
     * @return the pickled result.
     * @throws Exception
     */
    PF pickle(T t, PF target) throws Exception;

    /**
     * Unpickle a value from the specified pickle source.
     * @param source the pickle source.
     * @return the unpickled value.
     * @throws Exception
     */
    T unpickle(PF source) throws Exception;
}
