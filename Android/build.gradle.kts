// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Android Gradle Plugin sürümün olabilir farklı olabilir
        classpath ("com.android.tools.build:gradle:7.4.1")

        // Google Services plugin için classpath ekle
        classpath ("com.google.gms:google-services:4.4.3")
    }
}

