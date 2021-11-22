package eu.timerertim.knevo.serialization

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable

/**
 * Manages checkpoints by saving objects of the specified [class][cls] to a [directory] and deleting old objects so
 * there are at maximum [count] checkpoints identified by [name].
 */
class CheckpointManager<T : Serializable>
@JvmOverloads
@Throws(SecurityException::class)
constructor(
    private val cls: Class<T>,
    val directory: File,
    var count: Int,
    val name: String = "ckpt"
) {
    /**
     * Manages checkpoints by saving objects of the specified [class][cls] to a [directory] and deleting old objects so
     * there are at maximum [count] checkpoints identified by [name].
     */
    @JvmOverloads
    @Throws(SecurityException::class)
    constructor(cls: Class<T>, directory: String, count: Int, name: String = "ckpt") : this(
        cls, File(directory), count, name
    )

    init {
        directory.mkdirs()
    }

    /**
     * Loads the latest object of type [T] in the [directory] matching the configured [name].
     * Returns null if there is no such latest object in the directory.
     *
     * Can throw [ClassNotFoundException], [IOException], [SecurityException].
     */
    @Suppress("SwallowedException")
    @Throws(ClassNotFoundException::class, IOException::class, SecurityException::class)
    fun load(): T? {
        val latest = getLatestCheckpoint() ?: return null
        val target = File(directory, "${name}_$latest.knv")
        return try {
            FileInputStream(target).use {
                load(cls, it)
            }
        } catch (ex: FileNotFoundException) {
            null
        }
    }


    /**
     * Saves the object of type [T] to the [directory]. It creates a new file based on the [name] and the latest
     * checkpoint number. This method also deletes all previous checkpoints so that the last [count] checkpoints are
     * preserved. Returns the number of the checkpoint created.
     *
     * Can throw [IOException], [SecurityException].
     */
    @Throws(IOException::class, SecurityException::class)
    fun save(obj: T): Int {
        val latest = getLatestCheckpoint()
        val newLatest = (latest ?: -1) + 1

        val target = File(directory, "${name}_$newLatest.knv")
        FileOutputStream(target).use {
            obj.save(it)
        }

        val filesToDelete = directory.listFiles()?.mapNotNull { file ->
            val num = file.name.removePrefix("${name}_").removeSuffix(".knv").toIntOrNull()
                ?: return@mapNotNull null
            file to num
        }?.sortedBy { it.second }?.map { it.first }
        (if (count >= 0) filesToDelete?.dropLast(count) else emptyList())?.forEach(File::deleteRecursively)

        return newLatest
    }

    private fun getLatestCheckpoint() = directory.listFiles()?.mapNotNull {
        it.name.removePrefix("${name}_").removeSuffix(".knv").toIntOrNull()
    }?.maxOrNull()

    companion object {
        /**
         * Manages checkpoints by saving objects of type [T] to a [directory] and deleting old objects so
         * there are at maximum [count] checkpoints identified by [name].
         */
        inline operator fun <reified T : Serializable> invoke(directory: File, count: Int, name: String = "ckpt") =
            CheckpointManager(T::class.java, directory, count, name)

        /**
         * Manages checkpoints by saving objects of type [T] to a [directory] and deleting old objects so
         * there are at maximum [count] checkpoints identified by [name].
         */
        inline operator fun <reified T : Serializable> invoke(directory: String, count: Int, name: String = "ckpt") =
            CheckpointManager(T::class.java, directory, count, name)
    }
}