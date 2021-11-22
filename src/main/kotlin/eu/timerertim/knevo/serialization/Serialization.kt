@file:JvmName("Serialization")

package eu.timerertim.knevo.serialization

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InvalidClassException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.io.Serializable
import kotlin.reflect.jvm.jvmName

/**
 * Saves this object of type [T] to the given [output] without closing it. Can throw [IOException].
 */
@Throws(IOException::class)
fun <T : Serializable> T.save(output: OutputStream) = ObjectOutputStream(output).writeObject(this)

/**
 * Saves this object of type [T] to the given [path]. Can throw [IOException], [SecurityException].
 */
@Throws(IOException::class, SecurityException::class)
fun <T : Serializable> T.save(path: String) {
    val file = File(path)
    file.parentFile.mkdirs()
    FileOutputStream(file).use {
        save(it)
    }
}

/**
 * Loads an object of type [T] from the given [input] without closing it. Can throw [ClassNotFoundException],
 * [IOException].
 */
@Throws(ClassNotFoundException::class, IOException::class)
inline fun <reified T : Serializable> load(input: InputStream) = load(T::class.java, input)

/**
 * Loads an object of type [T] from the given [path]. Returns null if path describes a nonexistent file. Can throw
 * [ClassNotFoundException], [IOException], [SecurityException].
 */
@Throws(ClassNotFoundException::class, IOException::class, SecurityException::class)
inline fun <reified T : Serializable> load(path: String) = load(T::class.java, path)

/**
 * Loads an object of type [T] from the given [input] without closing it. Can throw [ClassNotFoundException],
 * [IOException].
 */
@Suppress("UNCHECKED_CAST")
@Throws(ClassNotFoundException::class, IOException::class)
fun <T : Serializable> load(cls: Class<T>, input: InputStream): T {
    val any = ObjectInputStream(input).readObject()
    return if (cls.isInstance(any)) any as T else throw InvalidClassException(
        "Class \"${any::class.jvmName}\" of loaded object does not match expected type \"${cls.name}\""
    )
}

/**
 * Loads an object of type [T] from the given [path]. Returns null if path describes a nonexistent file. Can throw
 * [ClassNotFoundException], [IOException], [SecurityException].
 */
@Throws(ClassNotFoundException::class, IOException::class, SecurityException::class)
@Suppress("SwallowedException")
fun <T : Serializable> load(cls: Class<T>, path: String): T? {
    return try {
        FileInputStream(path).use {
            load(cls, it)
        }
    } catch (ex: FileNotFoundException) {
        null
    }
}
