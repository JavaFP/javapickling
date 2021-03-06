javapickling
============

:warning: *__Note:__ JavaPickling has been superceded by [funcj.codec](
https://github.com/typemeta/funcj/tree/master/codec).*

**Java Pickling** is a Java framework for pickling Java object and values into target pickled formats,
and unpickling the results back into the original objects and values.
"Pickling" essentially means serialisation.
The approach and design were inspired by the [Pickling Combinators paper](http://research.microsoft.com/en-us/um/people/akenn/fun/picklercombinators.pdf), and by the [Scala Pickling project](https://github.com/scala/pickling).

The pickling is driven by the static types of values,
however it will also handle objects where the static type is unknown.
At present it does not provide automatic of pickling of custom classes,
ergo pickling of those types must be implemented by the user.
The custom picklers only need be defined once - the same pickler will be used regardless of the pickled format.

The design supports pickling into multiple pickled formats - JSON, XML and byte[] implementations are provided.

See ByteIOPicklerTest.java and JsonNodePicklerTest.java for example usage,
however once a PicklerCore set up this illustrates the basic API:
```java
void test(House house) {

    // Create a pickler.
    Pickler<House, JsonNode> pickler = jsonPickler.object_p(House.class);

    // Pickle a sample House object into a JsonNode.
    JsonNode node = pickler.pickle(house, null);

    // Unpickle the JsonNode back into a House.
    House house2 = pickler.unpickle(node);
}
```
## Quick Start

1. Add the javapickling jar to your build.
1. For any custom classes you wish to pickle implement a pickler class.
1. Either register your pickler class with the `PicklerCore` implementation you're about to use, or use the `DefaultPickler` annotation to specify your pickler.
1. Call `PicklerCore.pickle(obj, target)` to pickle an object into a pickled format, and `obj = PicklerCore.unpickler(source)` to reconstitute an object.

## Overview

The framework is based around two interfaces, described in more detail in the following sections.
The `Pickler` interface allows the pickling of types to be expressed, independently of a specific target format.
The `PicklerCore` interface provides a way for pickling into a specific format to be expressed as a set of Picklers for the base types.

Since this is based on a combinator framework, picklers are composable.
In particular, a pickler for a class will be composed of picklers corresponding to the field types of that class.

### Pickler
```java
public interface Pickler<T, PF> {
    PF pickle(T t, PF target) throws IOException;
    T unpickle(PF source) throws IOException;
}
```
A class which provides a pickling implementation for a class T implements `Pickler<T, PF>`.
The `PF` type parameter represents the pickled format type (such as JsonNode),
and remains a type parameter for the Pickler implementation class.

Pickler implementations generally sub-class `PicklerBase`
as this provides an implicit means of referencing the pickler methods in `PicklerCore`,
such as `string_p()` and `integer_p()`,
and allows picklers to be expressed more concisely.

### PicklerCore

A class which provides an implementation of pickling to a specific format implements `PicklerCore<PF>`,
where the PF type parameter specifies the pickled format. For example,
```java
public class JsonPicklerCore extends PicklerCoreBase<JsonNode> {
    // ...
}
```
Implementations of `PicklerCore<PF>` provide `Pickler<T, PF>`
implementations for the core types (primitives, collections and enums).
It also provides the tools required to facilitate implementing picklers for custom classes.

### Custom Class Pickling

"custom class" here means any class not directly supported by the framework.
Picklers for custom classes can take one of two forms.
If the class in question supports a direct conversion to a core type supported by the PicklerCore, such as `String`,
then the pickler can delegate directly to the pickler for that type.
E.g.
```java
@DefaultPickler(MyTypePickler.class)
public class MyType {
    public MyType(String s) {...}
    @Override public String toString() {...}
}

public class MyTypePickler<PF> extends PicklerBase<MyType, PF> {
    public MyTypePickler(PicklerCore<PF> core) {
        super(core);
    }

    @Override
    public PF pickle(MyType myType, PF target) throws IOException {
        return string_p().pickle(myType.toString(), target);
    }

    @Override
    public MyType unpickle(PF source) throws IOException {
        return new MyType(string_p().unpickle(source));
    }
}
```
If the class in question is more complex,
then the more general approach is to implement the pickler
as being composed of picklers for each field comprising the class. E.g.:
```java
@DefaultPickler(MyTypePickler.class)
public class MyType {
    public final Integer id;
    public final String name;

    public MyType(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}

public class MyTypePickler<PF> extends PicklerBase<MyType, PF> {
    private final Field<Integer, PF> id = field("id", integer_p());
    private final Field<String, PF> name = field("name", string_p());

    public MyTypePickler(PicklerCore<PF> core) {
        super(core);
    }

    @Override
    public PF pickle(MyType myType, PF target) throws IOException {
        final FieldPickler<PF> fp = object_map().pickler(target);
        fp.field(id, myType.id);
        fp.field(name, myType.name);
        return fp.pickle(target);
    }

    @Override
    public MyType unpickle(PF source) throws IOException {
        final FieldUnpickler<PF> fu = object_map().unpickler(source);
        return new MyType(
            fu.field(id),
            fu.field(name));
    }
}
```
A couple of things to note:
* The pickler for MyType is expressed in terms of the picklers for the field types which comprise MyType, namely Integer and String.
* The pickler is independent of the pickled format `PF`.

## Tutorial

The javapickling-json module contains a set of simple classes under the test/java/org/javapickling/tutorial directory.
The classes are as follows:
```java
public class Person implements Comparable<Person> {
    public final String name;
    public final boolean isFemale;
    public final Date dateOfBirth;

    public Person(String name, boolean isFemale, Date dateOfBirth) {
        this.name = name;
        this.isFemale = isFemale;
        this.dateOfBirth = dateOfBirth;
    }

    // ...
}

public class Team {
    public enum Role {ANALYST, DEVELOPER, TESTER};

    public final Optional<Person> leader;
    public final Map<Role, Set<Person>> members;

    public Team(Optional<Person> leader, Map<Role, Set<Person>> members) {
        this.leader = leader;
        this.members = members;
    }
}
```
The pickler class for `Person` is a generic class
which is parameterised with a `PF` type parameter and extends `PicklerBase<Person, PF>`:
```java
public class PersonPickler<PF> extends PicklerBase<Person, PF> {
```
`Person` is composed of 3 members, and `PersonPickler` needs a `Field` member for each `Person` member:
```java
    private final Field<String, PF> name = field("name", string_p());
    private final Field<Boolean, PF> isFemale = field("isFemale", boolean_p());
    private final Field<Date, PF> dateOfBirth = field("dateOfBirth", object_p(Date.class));
```
The constructor for `PersonPickler` just takes the `PicklerCore<PF>` and passes it to the base class constructor:
```java
    public PersonPickler(PicklerCore<PF> core) {
        super(core);
    }
```
The `pickle` method is responsible for pickling a `Person` object into the pickled format.
It does this by requesting a `FieldPickler` and then passing it each of the fields comprising `Person`:
```java
    @Override
    public PF pickle(Person person, PF target) throws Exception {
        final FieldPickler<PF> fp = object_map().pickler(target);
        fp.field(name,          person.name);
        fp.field(isFemale,      person.isFemale);
        fp.field(dateOfBirth,   person.dateOfBirth);
        return fp.pickle(target);
    }
```
The `unpickle` method is responsible for unpickling a `Person` object from the pickled format.
It does this by requesting a `FieldUnpickler` and extracting each field before passing them to the `Person` constructor.
```java
    @Override
    public Person unpickle(PF source) throws Exception {
        final FieldUnpickler<PF> fu = object_map().unpickler(source);
        return new Person(
                fu.field(name),
                fu.field(isFemale),
                fu.field(dateOfBirth)
            );
    }
}
```
In progress...

## History

(or why do we need another Java serialisation library/framework ?)

The library was born out the need for a Java serialisation framework that satisfied the following requirements:

1. Must be Java-based and able to serialise any Java type, including Collections, Enums, and all Generic types.
1. Multiple target formats must be supportable, with byte arrays and JSON being the initial set of target formats.
1. Boilerplate serialisation code for custom classes is acceptable but should be minimal, and should not have to be repeated for each target format.
1. Serialisers should be composable - it should be possible to express serialisers for classes as being composed of the serialisers for the constituent fields.
1. Reflection use should be minimised.
1. Serialisation should be driven by the Java types - static type information should be used when possible. Java code generation from IDL or similar is not allowed. Everything should be as strongly-typed as possible.
1. Performance should be reasonable - on a par with Java's built-in Serialisation (excluding Java Serialisation's initial Reflection-based start-up cost).

This seemed to rule out most, if not all, of the existing frameworks and libraries.
I was already familiar with Parser Combinator style frameworks having previously written a simple one in F#,
and wanted something similar for serialisers.

Around about this time the paper on the [Scala Pickling project](https://github.com/scala/pickling) came out.
This looked very interesting, though being Scala-based ruled it out.
Also the paper and the website documentation was light on implementation details,
but it did lead me to the [Pickling Combinators paper](http://research.microsoft.com/en-us/um/people/akenn/fun/picklercombinators.pdf),
which, although aimed at functional languages, had some interesting ideas which at first glance might translate to Java.
One weekend of coding later I had a working basic implementation.

## To-Do List

1. Unit tests:
    1. More comprehensive tests.
    1. Individual unit tests for picklers.
1. Reflection based generation of picklers for classes.
1. More Javadocs and general documentation.
