![Cognifide logo](http://cognifide.github.io/images/cognifide-logo.png)

# Gradle AEM Plugin

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/neva-dev/felix-search-webconsole-plugin.svg?label=License)](http://www.apache.org/licenses/)

## About

Currently there is no popular way to build applications for AEM using Gradle build system. This project contains brand new Gradle plugin to assemble CRX package and deploy it on instance(s).

Incremental build which takes seconds, not minutes. Developer who does not loose focus between build time gaps. Extend freely your build system directly in project. 

AEM developer - it's time to meet Gradle!

## Features

* Composing CRX package from multiple content roots, bundles.
* Easy multi-deployment with instance groups.
* Service component annotations processing (SCR).
* OSGi manifest customization by official 'osgi' plugin or feature rich 'org.dm.bundle' plugin.
* Automated dependant packages installation from local and remote sources.

## Configuration

Recommended way to start using Gradle AEM Plugin is to clone and customize [example project](https://github.com/Cognifide/gradle-aem-example).
All configuration options are listed [here](src/main/kotlin/com/cognifide/gradle/aem/AemConfig.kt).

Example configuration listed below assumes building project by single command `gradle contentDeploy` or just `gradle`.

### Root project (shared)

```
defaultTasks = ['contentDeploy']

plugins.withId 'cognifide.aem', {

    aem {
        config {
            contentPath = "src/main/content"
            instance("http://localhost:4502", "admin", "admin", "local-author")
            instance("http://localhost:4503", "admin", "admin", "local-publish")
        }
    }

}

```

### Sub project (specific)

```
defaultTasks = ['contentDeploy']

apply plugin: 'cognifide.aem'

aemSatisfy {
    local("pkg/apm-2.0.0.zip")
    // download("https://github.com/Cognifide/APM/releases/download/cqsm-2.0.0/apm-2.0.0.zip")
}

aemCompose {
    config {
        contentPath = "src/main/content"
    }

    includeProject ':example.bundle'
}

build.dependsOn aemCompose
task contentDeploy(dependsOn: [clean, build, aemDeploy])

```

## Tasks

* `aemCompose` - Compose CRX package from JCR content and bundles.
* `aemUpload` - Upload composed CRX package into AEM instance(s).
* `aemInstall` - Install uploaded CRX package on AEM instance(s).
* `aemActivate` - Replicate installed CRX package to other AEM instance(s).
* `aemDeploy` - Upload & install CRX package into AEM instance(s). Primary, recommended for of deployment. Optimized version of `aemUpload aemInstall`.
* `aemDistribute` - Upload, install & activate CRX package into AEM instances(s). Secondary form of deployment. Optimized version of `aemUpload aemInstall aemActivate -Paem.deploy.instance.group=*-author`.
* `aemSatisfy` - Upload & install dependant CRX package(s) before deployment.


### Command line:

* Deploying only to filtered group of instances

```
-Paem.deploy.instance.group=integration-*
-Paem.deploy.instance.group=*-author
```
   
* Deploying only to instances specified explicitly: 

```
-Paem.deploy.instance.list=http://localhost:4502,admin,admin;http://localhost:4503,admin,admin
```

* Skipping installed package resolution by download name (eliminating conflicts / only matters when Vault properties file is customized): 

```
-Paem.deploy.skipDownloadName=true`
```

## License

**Gradle AEM Plugin** is licensed under the [Apache License, Version 2.0 (the "License")](https://www.apache.org/licenses/LICENSE-2.0.txt)


