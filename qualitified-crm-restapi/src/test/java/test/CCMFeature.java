package test;
/*
 * (C) Copyright 2020 Qualitified.
 *
 * Contributors:
 *     Michael GENA
 */

import org.nuxeo.directory.test.DirectoryFeature;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.restapi.test.RestServerFeature;
import org.nuxeo.runtime.test.runner.*;

/**
 * Feature defined for CCM unit tests.
 */
@Features({RestServerFeature.class, CoreFeature.class, TransactionalFeature.class, PlatformFeature.class,
		AutomationFeature.class, DirectoryFeature.class})
//@Deploy("studio.extensions.crm")
@Deploy("org.qualitified.crm.qualitified-crm-restapi")
@Deploy("org.nuxeo.ecm.platform.test")
@Deploy("org.nuxeo.ecm.default.config")
@RepositoryConfig(cleanup = Granularity.METHOD)
public class CCMFeature implements RunnerFeature {


}