/*
 * Copyright 2017-present, Nam Seob Seo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */

package com.acrocontext.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class CreateKeyStoreTask extends DefaultTask {

    public static final String KEY_PASS = "pleasedonotuse"

    CreateKeyStoreTask() {
        description = "Creates a keystore with self-signed certificate"
        group = "certificate"
    }

    def outputDirectory = "$project.projectDir/build"
    def keystore = "$outputDirectory/keystore.jks"
    def keyStoreAlias
    def keyStoreDName

    @TaskAction
    def exec() {
        if (!project.file(keystore).exists()) {
            project.exec {
                commandLine = ['keytool', '-genkey',
                               '-storetype', 'PKCS12',
                               '-alias', keyStoreAlias,
                               '-keyalg', 'RSA',
                               '-keystore', keystore,
                               '-keysize', '2048',
                               '-dname', keyStoreDName,
                               '-keypass', KEY_PASS,
                               '-storepass', KEY_PASS,
                               '-validity', '365']
            }
        }
    }
}
