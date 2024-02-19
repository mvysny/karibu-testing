package com.github.mvysny.kaributesting.v10.mock

import com.vaadin.flow.function.DeploymentConfiguration
import java.io.File

public class FakeDeploymentConfiguration(public val delegate: DeploymentConfiguration) : DeploymentConfiguration by delegate {
    // workaround for https://github.com/vaadin/flow/issues/18682
    public override fun getProjectFolder(): File? = null
}
