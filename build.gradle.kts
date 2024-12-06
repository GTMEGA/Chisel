plugins {
    id("fpgradle-minecraft") version "0.8.2"
}

group = "team.chisel"

minecraft_fp {
    mod {
        modid = "chisel"
        name = "Chisel"
        rootPkg = "$group"
    }

    api {
        packages = listOf("api")
    }

    core {
        accessTransformerFile = "chisel_compile_at.cfg"
        coreModClass = "asm.CoreLoadingPlugin"
    }

    tokens {
        tokenClass = "Tags"
    }

    publish {
        maven {
            repoUrl = "https://mvn.falsepattern.com/gtmega_releases/"
            repoName = "gtmega"
        }
    }
}

repositories {
    exclusive(mavenpattern(), "com.falsepattern")
    exclusive(mega(), "codechicken")
    exclusive(maven("usrv", "https://mvn.falsepattern.com/usrv")) {
        includeModule("com.github.GTNewHorizons", "waila")
    }
    cursemavenEX()
}

dependencies {
    devOnlyNonPublishable("codechicken:notenoughitems-mc1.7.10:2.4.1-mega:dev")
    implementation("codechicken:codechickencore-mc1.7.10:1.4.0-mega:dev")

    compileOnly("com.falsepattern:falsetweaks-mc1.7.10:3.5.0:dev")
    compileOnly("codechicken:forgemultipart-mc1.7.10:1.6.2-mega:dev")

    compileOnly("curse.maven:ee3-65509:2305023")

    compileOnly("com.github.GTNewHorizons:waila:1.5.22:dev") {
        exclude("com.github.GTNewHorizons", "NotEnoughItems")
        exclude("com.github.GTNewHorizons", "CodeChickenLib")
    }
}