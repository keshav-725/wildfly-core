/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.test.integration.domain.suites;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.COMPOSITE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.EXTENSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.HOST_FAILURE_DESCRIPTIONS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MANAGEMENT_MAJOR_VERSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MANAGEMENT_MICRO_VERSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.MANAGEMENT_MINOR_VERSION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PROFILE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SERVER_GROUPS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.STEPS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;
import static org.jboss.as.test.integration.domain.management.util.DomainTestUtils.createCompositeOperation;
import static org.jboss.as.test.integration.domain.management.util.DomainTestUtils.createOperation;
import static org.jboss.as.test.integration.domain.management.util.DomainTestUtils.executeForFailure;
import static org.jboss.as.test.integration.domain.management.util.DomainTestUtils.executeForResult;
import static org.jboss.as.test.integration.domain.management.util.DomainTestUtils.exists;
import static org.jboss.as.test.integration.domain.management.util.DomainTestUtils.getRunningServerAddress;

import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.jboss.as.test.integration.domain.extension.ExtensionSetup;
import org.jboss.as.test.integration.domain.extension.VersionedExtensionCommon;
import org.jboss.as.test.integration.domain.management.util.DomainLifecycleUtil;
import org.jboss.as.test.integration.domain.management.util.DomainTestSupport;
import org.jboss.as.test.integration.domain.management.util.WildFlyManagedConfiguration;
import org.jboss.dmr.ModelNode;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Emanuel Muckenhuber
 */
public class OperationTransformationTestCase {

    private static DomainTestSupport testSupport;
    private static DomainLifecycleUtil primary;
    private static DomainLifecycleUtil secondary;

    @BeforeClass
    public static void setupDomain() throws Exception {
        testSupport = DomainTestSuite.createSupport(OperationTransformationTestCase.class.getSimpleName());

        primary = testSupport.getDomainPrimaryLifecycleUtil();
        secondary = testSupport.getDomainSecondaryLifecycleUtil();
        // Initialize the extension
        ExtensionSetup.initializeTransformersExtension(testSupport);
    }

    @AfterClass
    public static void tearDownDomain() throws Exception {
        DomainTestSuite.stopSupport();
        testSupport = null;
        primary = null;
        secondary = null;
    }

    @Test
    public void test() throws Exception {
        final PathAddress extension = PathAddress.pathAddress(PathElement.pathElement(EXTENSION, VersionedExtensionCommon.EXTENSION_NAME));
        final PathAddress address = PathAddress.pathAddress(PathElement.pathElement(PROFILE, "default"),
                PathElement.pathElement(SUBSYSTEM, VersionedExtensionCommon.SUBSYSTEM_NAME));
        final PathAddress ignored = PathAddress.pathAddress(PathElement.pathElement(PROFILE, "ignored"),
                PathElement.pathElement(SUBSYSTEM, VersionedExtensionCommon.SUBSYSTEM_NAME));

        final ModelNode serverAddress = getRunningServerAddress("secondary", "main-three");
        serverAddress.add(SUBSYSTEM, VersionedExtensionCommon.SUBSYSTEM_NAME);

        final DomainClient client = primary.getDomainClient();
        // Add extension
        final ModelNode extensionAdd = createAdd(extension);
        executeForResult(extensionAdd, client);
        // Add subsystem
        final ModelNode subsystemAdd = createAdd(address);
        executeForResult(subsystemAdd, client);
        // Check primary version
        final ModelNode mExt = create(READ_RESOURCE_OPERATION, extension.append(PathElement.pathElement(SUBSYSTEM, VersionedExtensionCommon.SUBSYSTEM_NAME)));
        mExt.get(INCLUDE_RUNTIME).set(true);
        assertVersion(executeForResult(mExt, client), ModelVersion.create(2));

        // Create subsystem in the ignored profile for further use
        final ModelNode newIgnored = createAdd(ignored);
        executeForResult(newIgnored, client);
        Assert.assertTrue(exists(ignored, client));

        // Check the new element
        final PathAddress newElement = address.append(PathElement.pathElement("new-element", "new1"));
        final ModelNode addNew = createAdd(newElement);
        executeForResult(addNew, client);
        Assert.assertTrue(exists(newElement, client));

        // And remove it again
        final ModelNode remove = addNew.clone();
        remove.get(OP).set(REMOVE);
        executeForResult(remove, client);
        Assert.assertFalse(exists(newElement, client));

        // The add operation on the secondary should have been discarded
        final ModelNode newElementOnSecondary = serverAddress.clone();
        newElementOnSecondary.add("new-element", "new1");
        Assert.assertFalse(exists(newElementOnSecondary, client));

        // Other new element
        final PathElement otherNewElementPath = PathElement.pathElement("other-new-element", "new1");
        // This needs to get rejected
        final ModelNode addOtherNew = createAdd(address.append(otherNewElementPath));
        final ModelNode otherFailure = executeForFailure(addOtherNew, client);

        Assert.assertTrue(otherFailure.hasDefined(HOST_FAILURE_DESCRIPTIONS));
        Assert.assertTrue(otherFailure.get(HOST_FAILURE_DESCRIPTIONS).hasDefined("secondary"));
        // Check that the host-failure contains WFLYCTL0304 rejected
        Assert.assertTrue(otherFailure.get(HOST_FAILURE_DESCRIPTIONS, "secondary").asString().contains("WFLYCTL0304"));

        // This should work
        final ModelNode addOtherNewIgnored = createAdd(ignored.append(otherNewElementPath));
        executeForResult(addOtherNewIgnored, client);

        // Same if wrapped in a composite
        final ModelNode cpa = createCompositeOperation(
                createAdd(address.append(PathElement.pathElement("new-element", "new2"))), // discard
                createAdd(ignored.append(PathElement.pathElement("other-new-element", "new2"))), // ignored
                createAdd(address.append(PathElement.pathElement("other-new-element", "new2")))); // fail
        final ModelNode compositeFailure = executeForFailure(cpa, client);
        Assert.assertTrue(compositeFailure.hasDefined(HOST_FAILURE_DESCRIPTIONS));
        Assert.assertTrue(compositeFailure.get(HOST_FAILURE_DESCRIPTIONS).hasDefined("secondary"));
        // Check that the host-failure contains WFLYCTL0304 rejected
        Assert.assertTrue(compositeFailure.get(HOST_FAILURE_DESCRIPTIONS, "secondary").asString().contains("WFLYCTL0304"));

        // Successful composite
        executeForResult(createCompositeOperation(
                createAdd(address.append(PathElement.pathElement("new-element", "new3"))), // discard
                createAdd(ignored.append(PathElement.pathElement("other-new-element", "new3"))) // ignored
        ), client);

        // Check the renamed element
        final PathAddress renamedAddress = address.append(PathElement.pathElement("renamed", "element"));
        final ModelNode renamedAdd = createAdd(renamedAddress);
        executeForResult(renamedAdd, client);
        Assert.assertTrue(exists(renamedAddress, client));

        // renamed > element
        final ModelNode renamedElementOnSecondary = serverAddress.clone();
        renamedElementOnSecondary.add("renamed", "element");
        Assert.assertFalse(exists(renamedElementOnSecondary, client));

        // element > renamed
        final ModelNode elementRenamedOnSecondary = serverAddress.clone();
        elementRenamedOnSecondary.add("element", "renamed");
        Assert.assertTrue(exists(elementRenamedOnSecondary, client));

//        final ModelNode op = create(READ_RESOURCE_OPERATION, PathAddress.pathAddress(PathElement.pathElement("profile", "ignored")));
//        System.out.println(executeForResult(op, secondary.getDomainClient()));

        final ModelNode update = new ModelNode();
        update.get(OP).set("update");
        update.get(OP_ADDR).set(address.toModelNode());

        //
        final ModelNode updateResult = client.execute(update);
        Assert.assertEquals(updateResult.toString(), updateResult.get(OUTCOME).asString(), SUCCESS);
        // "result" => {"test-attribute" => "test"},
        Assert.assertEquals("test", updateResult.get(RESULT, "test-attribute").asString());
        // server-result
        //Assert.assertEquals("test", updateResult.get(SERVER_GROUPS, "main-server-group", HOST, "secondary", "main-three", "response", RESULT, "test-attribute").asString());

        //
        final ModelNode write = new ModelNode();
        write.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
        write.get(OP_ADDR).set(address.toModelNode());
        write.get(NAME).set("test-attribute");
        write.get(VALUE).set("test123");

        //
        final ModelNode composite = new ModelNode();
        composite.get(OP).set(COMPOSITE);
        composite.get(OP_ADDR).setEmptyList();
        final ModelNode steps = composite.get(STEPS);

        final ModelNode test = new ModelNode();
        test.get(OP).set("test");
        test.get(OP_ADDR).set(serverAddress);

        steps.add(write);
        steps.add(test);


        final ModelNode compositeResult = client.execute(composite);
        // server-result
        Assert.assertEquals(false, compositeResult.get(SERVER_GROUPS, "main-server-group", HOST, "secondary", "main-three", "response", RESULT, "step-2", RESULT).asBoolean());

        // Test expression replacement
        testPropertiesModel();

        // verifies WFCORE-5675, we should not have WFLYPRT0018 due to Null Pointer Exceptions sending transformer operations
        List<String> linesFound = checkHostControllerLogFile(primary.getConfiguration(), "WFLYPRT0018");
        Assert.assertTrue("The secondary host-controller.log file contains unexpected warning errors: " + linesFound,
                linesFound.isEmpty());
    }

    protected void testPropertiesModel() throws Exception {
        final DomainClient client = primary.getDomainClient();
        final DomainClient secondaryClient = secondary.getDomainClient();

        final PathAddress address = PathAddress.pathAddress(PathElement.pathElement(PROFILE, "default"));

        // Test the properties model
        final PathAddress properties = address.append(PathElement.pathElement(SUBSYSTEM, VersionedExtensionCommon.SUBSYSTEM_NAME));

        final ModelNode writePropertiesInt = writeAttribute(properties, "int", "${org.jboss.domain.tests.int:1}");
        executeForFailure(writePropertiesInt, client);
        // Check both primary and secondary
        Assert.assertFalse(executeForResult(readAttribute(properties, "int"), client).isDefined());
        Assert.assertFalse(executeForResult(readAttribute(properties, "int"), secondaryClient).isDefined());

        final ModelNode writePropertiesString = writeAttribute(properties, "string", "${org.jboss.domain.tests.string:abc}");
        executeForFailure(writePropertiesString, client);
        // Check both primary and secondary
        Assert.assertFalse(executeForResult(readAttribute(properties, "string"), client).isDefined());
        Assert.assertFalse(executeForResult(readAttribute(properties, "string"), secondaryClient).isDefined());

        // Test the ignored model
        final PathAddress ignored = PathAddress.pathAddress(PathElement.pathElement(PROFILE, "ignored"), PathElement.pathElement(SUBSYSTEM, VersionedExtensionCommon.SUBSYSTEM_NAME));

        final ModelNode writeIgnoredString = writeAttribute(ignored, "string", "${org.jboss.domain.tests.string:abc}");
        executeForResult(writeIgnoredString, client);
        Assert.assertTrue(executeForResult(readAttribute(ignored, "string"), client).isDefined());
        executeForFailure(readAttribute(ignored, "string"), secondaryClient);

        final ModelNode writeIgnoredInt = writeAttribute(ignored, "int", "${org.jboss.domain.tests.int:1}");
        executeForResult(writeIgnoredInt, client);
        Assert.assertTrue(executeForResult(readAttribute(ignored, "int"), client).isDefined());
        executeForFailure(readAttribute(ignored, "int"), secondaryClient);
    }

    static ModelNode createAdd(PathAddress address) {
        return create(ADD, address);
    }

    static ModelNode create(String op, ModelNode address) {
        return createOperation(op, address);
    }

    static ModelNode create(String op, PathAddress address) {
        return createOperation(op, address);
    }

    static ModelNode writeAttribute(PathAddress address, String name, int value) {
        final ModelNode operation = writeAttribute(address, name);
        operation.get(VALUE).set(value);
        return operation;
    }

    static ModelNode writeAttribute(PathAddress address, String name, String value) {
        final ModelNode operation = writeAttribute(address, name);
        operation.get(VALUE).set(value);
        return operation;
    }

    static ModelNode readAttribute(PathAddress address, String name) {
        final ModelNode operation = createOperation(READ_ATTRIBUTE_OPERATION, address);
        operation.get(NAME).set(name);
        return operation;
    }

    static ModelNode writeAttribute(PathAddress address, String name) {
        final ModelNode operation =  createOperation(WRITE_ATTRIBUTE_OPERATION, address);
        operation.get(NAME).set(name);
        return operation;
    }

    static void assertVersion(final ModelNode v, ModelVersion version) {
        Assert.assertEquals(version.getMajor(), v.get(MANAGEMENT_MAJOR_VERSION).asInt());
        Assert.assertEquals(version.getMinor(), v.get(MANAGEMENT_MINOR_VERSION).asInt());
        Assert.assertEquals(version.getMicro(), v.get(MANAGEMENT_MICRO_VERSION).asInt());
    }

    private List<String> checkHostControllerLogFile(WildFlyManagedConfiguration appConfiguration, String containString) throws IOException {
        final File logFile = new File(appConfiguration.getDomainDirectory(), "log" + File.separator + "host-controller.log");
        if (!logFile.exists()) {
            throw new IOException("Log file '" + logFile + "' does not exist");
        }
        List<String> linesFound = new ArrayList<>();
        try (Reader reader = new FileReader(logFile); BufferedReader in = new BufferedReader(reader)) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains(containString)) {
                    linesFound.add(line);
                }
            }
        }
        return linesFound;
    }
}
