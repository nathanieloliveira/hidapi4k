package io.github.nathanieloliveira.hidapi4k

import java.io.File
import java.lang.foreign.Arena
import java.lang.foreign.Linker
import java.lang.foreign.SymbolLookup
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object Loader {

    const val HIDAPI4K_LIBRARY_PATH = "HIDAPI4K_LIBRARY_PATH"

    val dataPath: String get() = System.getProperty("hid4k.data.path") ?: "${System.getProperty("user.home")}/.hid4k/"

    val arena = Arena.ofAuto()
    val linker = Linker.nativeLinker()
    lateinit var lib: SymbolLookup

    private fun unpackIfNeeded(dest: File, resourceName: String, deleteOnExit: Boolean): File {
        val file = File(dest, resourceName)
        if (!file.exists()) {
            if (file.exists()) return file
            val tempFile = File.createTempFile("skiko", "", dest)
            if (deleteOnExit)
                file.deleteOnExit()
            Loader::class.java.getResourceAsStream("/$resourceName")!!.use { input ->
                Files.copy(input, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE)
        }
        return file
    }


    fun load() {
        val libraryName = System.getProperty(HIDAPI4K_LIBRARY_PATH)
        if (libraryName != null && libraryName.isNotBlank()) {
            System.load(libraryName)
            lib = SymbolLookup.libraryLookup(libraryName, arena)
            return
        }

        val dataDir = File(File(dataPath), "hidapi")
        dataDir.mkdirs()

        val platformName = System.mapLibraryName("hidapi")
        val library = unpackIfNeeded(dataDir, platformName, false)
//        System.load(library.absolutePath)

        lib = SymbolLookup.libraryLookup(library.absolutePath, arena)
    }

}