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
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

class ExportCertificateTask extends DefaultTask {

    ExportCertificateTask() {
        description = "Exports the certificate"
        group = "certificate"
    }

    @Internal
    def keyStoreAlias
    @Internal
    def outputDirectory = "$project.projectDir/build"
    @Internal
    def keystore = "$outputDirectory/keystore.jks"
    @Internal
    def cert = "$outputDirectory/cert.crt"

    @TaskAction
    def exec() {
        if (!project.file(cert).exists()) {
            project.exec {
                commandLine = ['keytool', '-export',
                               '-alias', keyStoreAlias,
                               '-file', cert,
                               '-keystore', keystore,
                               '-keypass', CreateKeyStoreTask.KEY_PASS,
                               '-storepass', CreateKeyStoreTask.KEY_PASS]
            }
        }
    }
}
