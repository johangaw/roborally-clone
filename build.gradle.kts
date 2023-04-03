import com.soywiz.korge.gradle.*

plugins {
    alias(libs.plugins.korge)
//    kotlin("plugin.serialization") version "1.7.21"  // Need to use same kotlin-plugin version as korge
}

korge {
    id = "com.example.example"
    //supportBox2d()
// To enable all targets at once

    //targetAll()

// To enable targets based on properties/environment variables
    //targetDefault()

// To selectively enable targets

    targetJvm()
//	targetJs()
    //targetDesktop()
    //targetIos()
    //targetAndroidIndirect() // targetAndroidDirect()

    serializationJson()
}
