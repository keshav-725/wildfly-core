/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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

package org.wildfly.extension.elytron._private;

import static org.jboss.logging.Logger.Level.WARN;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;
import java.security.Policy;
import java.security.Provider;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.ExpressionResolver;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.Param;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController.State;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartException;
import org.wildfly.security.auth.server.SecurityRealm;
import org.wildfly.security.x500.cert.acme.AcmeException;

/**
 * Messages for the Elytron subsystem.
 *
 * <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 */
@MessageLogger(projectCode = "WFLYELY", length = 5)
public interface ElytronSubsystemMessages extends BasicLogger {

    /**
     * A root logger with the category of the package name.
     */
    ElytronSubsystemMessages ROOT_LOGGER = Logger.getMessageLogger(ElytronSubsystemMessages.class, "org.wildfly.extension.elytron");

    /**
     * {@link OperationFailedException} if the same realm is injected multiple times for a single domain.
     *
     * @param realmName - the name of the {@link SecurityRealm} being injected.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 2, value = "Can not inject the same realm '%s' in a single security domain.")
    OperationFailedException duplicateRealmInjection(final String realmName);

    /**
     * An {@link IllegalArgumentException} if the supplied operation did not contain an address with a value for the required key.
     *
     * @param key - the required key in the address of the operation.
     * @return The {@link IllegalArgumentException} for the error.
     */
    @Message(id = 3, value = "The operation did not contain an address with a value for '%s'.")
    IllegalArgumentException operationAddressMissingKey(final String key);

    /**
     * A {@link StartException} if it is not possible to initialise the {@link Service}.
     *
     * @param cause the cause of the failure.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 4, value = "Unable to start the service.")
    StartException unableToStartService(@Cause Exception cause);

    /**
     * An {@link OperationFailedException} if it is not possible to access the {@link KeyStore} at RUNTIME.
     *
     * @param cause the underlying cause of the failure
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 5, value = "Unable to access KeyStore to complete the requested operation.")
    OperationFailedException unableToAccessKeyStore(@Cause Exception cause);

//    /**
//     * An {@link OperationFailedException} for operations that are unable to populate the result.
//     *
//     * @param cause the underlying cause of the failure.
//     * @return The {@link OperationFailedException} for the error.
//     */
//    @Message(id = 6, value = "Unable to populate result.")
//    OperationFailedException unableToPopulateResult(@Cause Exception cause);

    /**
     * An {@link OperationFailedException} where an operation can not proceed as it's required service is not UP.
     *
     * @param serviceName the name of the service that is required.
     * @param state the actual state of the service.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 7, value = "The required service '%s' is not UP, it is currently '%s'.")
    OperationFailedException requiredServiceNotUp(ServiceName serviceName, State state);

    /**
     * An {@link OperationFailedException} where the name of the operation does not match the expected names.
     *
     * @param actualName the operation name contained within the request.
     * @param expectedNames the expected operation names.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 8, value = "Invalid operation name '%s', expected one of '%s'")
    OperationFailedException invalidOperationName(String actualName, String... expectedNames);

    /**
     * An {@link RuntimeException} where an operation can not be completed.
     *
     * @param cause the underlying cause of the failure.
     * @return The {@link RuntimeException} for the error.
     */
    @Message(id = 9, value = "Unable to complete operation. '%s'")
    RuntimeException unableToCompleteOperation(@Cause Throwable cause, String causeMessage);

    /**
     * An {@link OperationFailedException} where this an attempt to save a KeyStore without a File defined.
     *
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 10, value = "Unable to save KeyStore - KeyStore file '%s' does not exist.")
    OperationFailedException cantSaveWithoutFile(final String file);

//    /**
//     * A {@link StartException} for when provider registration fails due to an existing registration.
//     *
//     * @param name the name of the provider registration failed for.
//     * @return The {@link StartException} for the error.
//     */
//    @Message(id = 11, value = "A Provider is already registered for '%s'")
//    StartException providerAlreadyRegistered(String name);

    /**
     * A {@link StartException} where a service can not identify a suitable {@link Provider}
     *
     * @param type the type being searched for.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 12, value = "No suitable provider found for type '%s'")
    StartException noSuitableProvider(String type);

    /**
     * A {@link OperationFailedException} for when an attempt is made to define a domain that has a default realm specified that
     * it does not actually reference.
     *
     * @param defaultRealm the name of the default_realm specified.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 13, value = "The default-realm '%s' is not in the list of realms [%s] referenced by this domain.")
    OperationFailedException defaultRealmNotReferenced(String defaultRealm, String realms);

    /**
     * A {@link StartException} for when the properties file backed realm can not be started due to problems loading the
     * properties files.
     *
     * @param cause the underlying cause of the error.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 14, value = "Unable to load the properties files required to start the properties file backed realm: Users file: '%s' Groups file: '%s'")
    StartException unableToLoadPropertiesFiles(@Cause Exception cause, String usersFile, String groupsFile);

    /**
     * A {@link StartException} where a custom component has been defined with configuration but does not implement
     * the {@code initialize(Map<String, String>)} method.
     *
     * @param className the class name of the custom component implementation being loaded.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 15, value = "The custom component implementation '%s' does not implement method initialize(Map<String, String>), however configuration has been supplied.")
    StartException componentNotConfigurable(final String className, @Cause Exception cause);

    /**
     * An {@link OperationFailedException} where validation of a specified regular expression has failed.
     *
     * @param pattern the regular expression that failed validation.
     * @param cause the reported {@link Exception} during validation.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 16, value = "The supplied regular expression '%s' is invalid.")
    OperationFailedException invalidRegularExpression(String pattern, @Cause Exception cause);

    /**
     * A {@link StartException} for when the properties file backed realm can not be used because property file(s) does not exist.
     *
     * @param file the missing file detail.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 17, value = "Property file referenced in properties-realm does not exist: %s")
    StartException propertyFilesDoesNotExist(String file);

    /**
     * A {@link StartException} where a Key or Trust manager factory can not be created for a specific algorithm.
     *
     * @param type the type of manager factory being created.
     * @param algorithm the requested algorithm.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 18, value = "Unable to create %s for algorithm '%s'.")
    StartException unableToCreateManagerFactory(final String type, final String algorithm);

    /**
     * A {@link StartException} where a specific type can not be found in an injected value.
     *
     * @param type the type required.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 19, value = "No '%s' found in injected value.")
    StartException noTypeFound(final String type);

    /**
     * A {@link OperationFailedException} for when the properties file used by the realm can not be reloaded.
     *
     * @param cause the underlying cause of the error.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 20, value = "Unable to reload the properties files required to by the properties file backed realm.")
    OperationFailedException unableToReLoadPropertiesFiles(@Cause Exception cause);

    /**
     * A {@link StartException} for when creating of the {@link java.security.Permission} will fail.
     *
     * @param permissionClassName class-name of the created permission
     * @param cause the underlying cause of the error.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 21, value = "Exception while creating the permission object for the permission mapping. Please check [class-name], [target-name] (name of permission) and [action] of [%s].")
    StartException exceptionWhileCreatingPermission(String permissionClassName, @Cause Throwable cause);

    @Message(id = 22, value = "KeyStore file '%s' does not exist and required.")
    StartException keyStoreFileNotExists(final String file);

    @LogMessage(level = WARN)
    @Message(id = 23, value = "KeyStore file '%s' does not exist. Used blank.")
    void keyStoreFileNotExistsButIgnored(final String file);

    @LogMessage(level = WARN)
    @Message(id = 24, value = "Certificate [%s] in KeyStore is not valid")
    void certificateNotValid(String alias, @Cause Exception cause);

    @Message(id = 25, value = "Referenced property file is invalid: %s")
    StartException propertyFileIsInvalid(String message, @Cause Throwable cause);

    //@Message(id = 26, value = "trusted-security-domains cannot contain the security-domain '%s' itself")
    //OperationFailedException trustedDomainsCannotContainDomainItself(String domain);

    @Message(id = 27, value = "Unable to obtain OID for X.500 attribute '%s'")
    OperationFailedException unableToObtainOidForX500Attribute(String attribute);

    @Message(id = 28, value = "The X.500 attribute must be defined by name or by OID")
    OperationFailedException x500AttributeMustBeDefined();

    @Message(id = 29, value = "Failed to parse URL '%s'")
    OperationFailedException invalidURL(String url, @Cause Exception cause);

    @Message(id = 30, value = "Realm '%s' does not support cache")
    StartException realmDoesNotSupportCache(String realmName);

    @Message(id = 31, value = "Unable to access CRL file.")
    StartException unableToAccessCRL(@Cause Exception cause);

    @Message(id = 32, value = "Unable to reload CRL file.")
    RuntimeException unableToReloadCRL(@Cause Exception cause);

    /**
     * A {@link RuntimeException} if it is not possible to access an entry from a {@link KeyStore} at RUNTIME.
     *
     * @param alias the entry that couldn't be accessed
     * @param keyStore the keystore
     * @return {@link RuntimeException} for the error.
     */
    @Message(id = 33, value = "Unable to access entry [%s] from key store [%s].")
    RuntimeException unableToAccessEntryFromKeyStore(String alias, String keyStore);

    @Message(id = 34, value = "A principal query can only have a single key mapper")
    OperationFailedException jdbcRealmOnlySingleKeyMapperAllowed();

    @Message(id = 35, value = "Unable to load module '%s'.")
    OperationFailedException unableToLoadModule(String module, @Cause Exception cause);

    /**
     * A {@link OperationFailedException} for when validating a security domain fails due to the same realm being referenced twice.
     *
     * @param realmName the name of the security realm referenced twice.
     * @return The {@link OperationFailedException} for the error.
     */
    @Message(id = 36, value = "Security realm '%s' has been referenced twice in the same security domain.")
    OperationFailedException realmRefererencedTwice(String realmName);

    /**
     * A {@link StartException} where a specific type is not type of injected value.
     *
     * @param type the type required.
     * @return The {@link StartException} for the error.
     */
    @Message(id = 37, value = "Injected value is not of '%s' type.")
    StartException invalidTypeInjected(final String type);

    @Message(id = 38, value = "Could not load permission class '%s'")
    StartException invalidPermissionClass(String className);

    @Message(id = 39, value = "Unable to reload CRL file - TrustManager is not reloadable")
    OperationFailedException unableToReloadCRLNotReloadable();

    @Message(id = 40, value = "Unable to load the permission module '%s' for the permission mapping")
    StartException invalidPermissionModule(String module, @Cause Throwable cause);

    @Message(id = 41, value = "Unable to transform configuration to the target version - attribute '%s' is different from '%s'")
    String unableToTransformTornAttribute(String attribute1, String attribute2);

    @Message(id = 42, value = "Unable to transform multiple 'authorization-realms' to the single value")
    String unableToTransformMultipleRealms();

    @Message(id = 43, value = "A cycle has been detected initialising the resources - %s")
    OperationFailedException cycleDetected(String cycle);

    @Message(id = 44, value = "Unexpected name of servicename's parent - %s")
    IllegalStateException invalidServiceNameParent(String canonicalName);

    @Message(id = 45, value = "Failed to load CallbackHandler from the provided module.")
    IllegalStateException failedToLoadCallbackhandlerFromProvidedModule();

    @Message(id = 46, value = "Provided path '%s' to JAAS configuration file does not exist.")
    StartException jaasFileDoesNotExist(final String path);

    @LogMessage(level = WARN)
    @Message(id = 47, value = "LDAP Realm is configured to use direct-verification and user-password-mapper which is invalid configuration.")
    void ldapRealmDirectVerificationAndUserPasswordMapper();

    @Message(id = 48, value = "A string representation of an X.500 distinguished name is required: %s")
    IllegalArgumentException representationOfX500IsRequired(String causeMessage);

    /*
     * Credential Store Section.
     */

    @Message(id = 909, value = "Credential store '%s' does not support given credential store entry type '%s'")
    OperationFailedException credentialStoreEntryTypeNotSupported(String credentialStoreName, String entryType);

    @Message(id = 910, value = "Password cannot be resolved for key-store '%s'")
    IOException keyStorePasswordCannotBeResolved(String path);

    @Message(id = 911, value = "Credential store '%s' protection parameter cannot be resolved")
    IOException credentialStoreProtectionParameterCannotBeResolved(String name);

//    @LogMessage(level = ERROR)
//    @Message(id = 912, value = "Credential store issue encountered")
//    void credentialStoreIssueEncountered(@Cause Exception cause);

    @Message(id = 913, value = "Credential alias '%s' of credential type '%s' already exists in the store")
    OperationFailedException credentialAlreadyExists(String alias, String credentialType);

    @Message(id = 914, value = "Provider loader '%s' cannot supply Credential Store provider of type '%s'")
    NoSuchProviderException providerLoaderCannotSupplyProvider(String providerLoader, String type);

//    @Message(id = 915, value = "Name of the credential store has to be specified in this credential-reference")
//    IllegalStateException nameOfCredentialStoreHasToBeSpecified();

    @Message(id = 916, value = "Credential cannot be resolved")
    IllegalStateException credentialCannotBeResolved();

    @Message(id = 917, value = "Password cannot be resolved for dir-context")
    StartException dirContextPasswordCannotBeResolved(@Cause Exception cause);

    @Message(id = 920, value = "Credential alias '%s' of credential type '%s' does not exist in the store")
    OperationFailedException credentialDoesNotExist(String alias, String credentialType);

    @Message(id = 921, value = "Location parameter is not specified for filebased keystore type '%s'")
    OperationFailedException filebasedKeystoreLocationMissing(String type);

    @Message(id = Message.NONE, value = "Reload dependent services which might already have cached the secret value")
    String reloadDependantServices();

    @Message(id = Message.NONE, value = "Update dependent resources as alias '%s' does not exist anymore")
    String updateDependantServices(String alias);

    @Message(id = 922, value = "Unable to load credential from credential store.")
    ExpressionResolver.ExpressionResolutionUserException unableToLoadCredential(@Cause Throwable cause);

    @Message(id = 923, value = "Unable to encrypt the supplied clear text.")
    OperationFailedException unableToEncryptClearText(@Cause Throwable cause);

    @Message(id = 924, value = "Unable to create immediately available credential store.")
    OperationFailedException unableToCreateCredentialStoreImmediately(@Cause Throwable cause);

    @Message(id = 925, value = "Unable to reload the credential store.")
    OperationFailedException unableToReloadCredentialStore(@Cause Throwable cause);

    @Message(id = 926, value = "Unable to initialize the credential store.")
    OperationFailedException unableToInitialiseCredentialStore(@Cause Throwable cause);

    @Message(id = 927, value = "The secret key operation '%s' failed to complete due to '%s'.")
    OperationFailedException secretKeyOperationFailed(String operationName, String error, @Cause Throwable cause);

    /*
     * Identity Resource Messages - 1000
     */

    @Message(id = 1000, value = "Identity with name [%s] already exists.")
    OperationFailedException identityAlreadyExists(final String principalName);

    @Message(id = 1001, value = "Could not create identity with name [%s].")
    RuntimeException couldNotCreateIdentity(final String principalName, @Cause Exception cause);

    @Message(id = 1002, value = "Identity with name [%s] not found.")
    String identityNotFound(final String principalName);

    @Message(id = 1003, value = "Could not delete identity with name [%s].")
    RuntimeException couldNotDeleteIdentity(final String principalName, @Cause Exception cause);

    @Message(id = 1004, value = "Identity with name [%s] not authorized.")
    String identityNotAuthorized(final String principalName);

    @Message(id = 1005, value = "Could not read identity [%s] from security domain [%s].")
    RuntimeException couldNotReadIdentity(final String principalName, final ServiceName domainServiceName, @Cause Exception cause);

//    @Message(id = 1006, value = "Unsupported password type [%s].")
//    RuntimeException unsupportedPasswordType(final Class passwordType);

    @Message(id = 1007, value = "Could not read identity with name [%s].")
    RuntimeException couldNotReadIdentity(final String principalName, @Cause Exception cause);

    @Message(id = 1008, value = "Failed to obtain the authorization identity.")
    RuntimeException couldNotObtainAuthorizationIdentity(@Cause Exception cause);

    @Message(id = 1009, value = "Failed to add attribute.")
    RuntimeException couldNotAddAttribute(@Cause Exception cause);

    @Message(id = 1010, value = "Failed to remove attribute.")
    RuntimeException couldNotRemoveAttribute(@Cause Exception cause);

    @Message(id = 1011, value = "Could not create password.")
    RuntimeException couldNotCreatePassword(@Cause Exception cause);

    @Message(id = 1012, value = "Unexpected password type [%s].")
    OperationFailedException unexpectedPasswordType(final String passwordType);

    @Message(id = 1013, value = "Pattern [%s] requires a capture group")
    OperationFailedException patternRequiresCaptureGroup(final String pattern);

    @Message(id = 1014, value = "Invalid [%s] definition. Only one of '%s' or '%s' can be set in one Object in the list of filters.")
    OperationFailedException invalidDefinition(final String property, String filterNameOne, String filterNameTwo);

    @Message(id = 1015, value = "Unable to perform automatic outflow for '%s'")
    IllegalStateException unableToPerformOutflow(String identityName, @Cause Exception cause);

    @Message(id = 1016, value = "Server '%s' not known")
    OperationFailedException serverNotKnown(final String server, @Cause UnknownHostException e);

    @Message(id = 1017, value = "Invalid value for cipher-suite-filter. %s")
    OperationFailedException invalidCipherSuiteFilter(@Cause Throwable cause, String causeMessage);

    @Message(id = 1018, value = "Invalid size %s")
    OperationFailedException invalidSize(String size);

    @Message(id = 1019, value = "The suffix (%s) can not contain seconds or milliseconds.")
    OperationFailedException suffixContainsMillis(String suffix);

    @Message(id = 1020, value = "The suffix (%s) is invalid. A suffix must be a valid date format.")
    OperationFailedException invalidSuffix(String suffix);

//    @Message(id = 1021, value = "Cannot remove the default policy provider [%s]")
//    OperationFailedException cannotRemoveDefaultPolicy(String defaultPolicy);

    @Message(id = 1022, value = "Failed to set policy [%s]")
    RuntimeException failedToSetPolicy(Policy policy, @Cause Exception cause);

    @Message(id = 1023, value = "Could not find policy provider with name [%s]")
    XMLStreamException cannotFindPolicyProvider(String policyProvider, @Param Location location);

    @Message(id = 1024, value = "Failed to register policy context handlers")
    RuntimeException failedToRegisterPolicyHandlers(@Cause Exception cause);

    @Message(id = 1025, value = "Failed to create policy [%s]")
    RuntimeException failedToCreatePolicy(String className, @Cause Exception cause);

    @LogMessage(level = WARN)
    @Message(id = 1026, value = "Element '%s' with attribute '%s' set to '%s' is unused. Since unused policy " +
            "configurations can no longer be stored in the configuration model this item is being discarded.")
    void discardingUnusedPolicy(String element, String attr, String name);

    @Message(id = 1027, value = "Key password cannot be resolved for key-store '%s'")
    IOException keyPasswordCannotBeResolved(String path);

    @Message(id = 1028, value = "Invalid value for not-before. %s")
    OperationFailedException invalidNotBefore(@Cause Throwable cause, String causeMessage);

    @Message(id = 1029, value = "Alias '%s' does not exist in KeyStore")
    OperationFailedException keyStoreAliasDoesNotExist(String alias);

    @Message(id = 1030, value = "Alias '%s' does not identify a PrivateKeyEntry in KeyStore")
    OperationFailedException keyStoreAliasDoesNotIdentifyPrivateKeyEntry(String alias);

    @Message(id = 1031, value = "Unable to obtain PrivateKey for alias '%s'")
    OperationFailedException unableToObtainPrivateKey(String alias);

    @Message(id = 1032, value = "Unable to obtain Certificate for alias '%s'")
    OperationFailedException unableToObtainCertificate(String alias);

    @Message(id = 1033, value = "No certificates found in certificate reply")
    OperationFailedException noCertificatesFoundInCertificateReply();

    @Message(id = 1034, value = "Public key from certificate reply does not match public key from certificate in KeyStore")
    OperationFailedException publicKeyFromCertificateReplyDoesNotMatchKeyStore();

    @Message(id = 1035, value = "Certificate reply is the same as the certificate from PrivateKeyEntry in KeyStore")
    OperationFailedException certificateReplySameAsCertificateFromKeyStore();

    @Message(id = 1036, value = "Alias '%s' already exists in KeyStore")
    OperationFailedException keyStoreAliasAlreadyExists(String alias);

    @Message(id = 1037, value = "Top-most certificate from certificate reply is not trusted. Inspect the certificate carefully and if it is valid, execute import-certificate again with validate set to false.")
    OperationFailedException topMostCertificateFromCertificateReplyNotTrusted();

    @Message(id = 1038, value = "Trusted certificate is already in KeyStore under alias '%s'")
    OperationFailedException trustedCertificateAlreadyInKeyStore(String alias);

    @Message(id = 1039, value = "Trusted certificate is already in cacerts KeyStore under alias '%s'")
    OperationFailedException trustedCertificateAlreadyInCacertsKeyStore(String alias);

    @Message(id = 1040, value = "Unable to determine if the certificate is trusted. Inspect the certificate carefully and if it is valid, execute import-certificate again with validate set to false.")
    OperationFailedException unableToDetermineIfCertificateIsTrusted();

    @Message(id = 1041, value = "Certificate file does not exist")
    OperationFailedException certificateFileDoesNotExist(@Cause Exception cause);

    @Message(id = 1042, value = "Unable to obtain Entry for alias '%s'")
    OperationFailedException unableToObtainEntry(String alias);

    @Message(id = 1043, value = "Unable to create an account with the certificate authority: %s")
    OperationFailedException unableToCreateAccountWithCertificateAuthority(@Cause Exception cause, String causeMessage);

    @Message(id = 1044, value = "Unable to change the account key associated with the certificate authority: %s")
    OperationFailedException unableToChangeAccountKeyWithCertificateAuthority(@Cause Exception cause, String causeMessage);

    @Message(id = 1045, value = "Unable to deactivate the account associated with the certificate authority: %s")
    OperationFailedException unableToDeactivateAccountWithCertificateAuthority(@Cause Exception cause, String causeMessage);

    @Message(id = 1046, value = "Unable to obtain certificate authority account Certificate for alias '%s'")
    StartException unableToObtainCertificateAuthorityAccountCertificate(String alias);

    @Message(id = 1047, value = "Unable to obtain certificate authority account PrivateKey for alias '%s'")
    StartException unableToObtainCertificateAuthorityAccountPrivateKey(String alias);

    @Message(id = 1048, value = "Unable to update certificate authority account key store: %s")
    OperationFailedException unableToUpdateCertificateAuthorityAccountKeyStore(@Cause Exception cause, String causeMessage);

    @Message(id = 1049, value = "Unable to respond to challenge from certificate authority: %s")
    AcmeException unableToRespondToCertificateAuthorityChallenge(@Cause Exception cause, String causeMessage);

    @Message(id = 1050, value = "Invalid certificate authority challenge")
    AcmeException invalidCertificateAuthorityChallenge();

    @Message(id = 1051, value = "Invalid certificate revocation reason '%s'")
    OperationFailedException invalidCertificateRevocationReason(String reason);

    @Message(id = 1052, value = "Unable to instantiate AcmeClientSpi implementation")
    IllegalStateException unableToInstatiateAcmeClientSpiImplementation();

    @Message(id = 1053, value = "Unable to update the account with the certificate authority: %s")
    OperationFailedException unableToUpdateAccountWithCertificateAuthority(@Cause Exception cause, String causeMessage);

    @Message(id = 1054, value = "Unable to get the metadata associated with the certificate authority: %s")
    OperationFailedException unableToGetCertificateAuthorityMetadata(@Cause Exception cause, String causeMessage);

    @Message(id = 1055, value = "Invalid key size: %d")
    OperationFailedException invalidKeySize(int keySize);

    @Message(id = 1056, value = "A certificate authority account with this account key already exists. To update the contact" +
            " information associated with this existing account, use %s. To change the key associated with this existing account, use %s.")
    OperationFailedException certificateAuthorityAccountAlreadyExists(String updateAccount, String changeAccountKey);

    @Message(id = 1057, value = "Failed to create ServerAuthModule [%s] using module '%s'")
    RuntimeException failedToCreateServerAuthModule(String className, String module, @Cause Exception cause);

    @Message(id = 1058, value = "Failed to parse PEM public key with kid: %s")
    OperationFailedException failedToParsePEMPublicKey(String kid);

    @Message(id = 1059, value = "Unable to detect KeyStore '%s'")
    StartException unableToDetectKeyStore(String path);

    @Message(id = 1060, value = "Fileless KeyStore needs to have a defined type.")
    OperationFailedException filelessKeyStoreMissingType();

    @Message(id = 1061, value = "Invalid value of host context map: '%s' is not valid hostname pattern.")
    OperationFailedException invalidHostContextMapValue(String hostname);

    @Message(id = 1062, value = "Value for attribute '%s' is invalid.")
    OperationFailedException invalidAttributeValue(String attributeName);

    @Message(id = 1063, value = "LetsEncrypt certificate authority is configured by default.")
    OperationFailedException letsEncryptNameNotAllowed();

    @Message(id = 1064, value = "Failed to load OCSP responder certificate '%s'.")
    StartException failedToLoadResponderCert(String alias, @Cause Exception exception);

    @Message(id = 1065, value = "Multiple maximum-cert-path definitions found.")
    OperationFailedException multipleMaximumCertPathDefinitions();

    @Message(id = 1066, value = "Invalid value for cipher-suite-names. %s")
    OperationFailedException invalidCipherSuiteNames(@Cause Throwable cause, String causeMessage);

    @Message(id = 1067, value = "Value '%s' is not valid regex.")
    OperationFailedException invalidRegex(String regex);

    @Message(id = 1068, value = "Duplicate PolicyContextHandler found for key '%s'.")
    IllegalStateException duplicatePolicyContextHandler(String key);

    @Message(id = 1069, value = "Invalid %s loaded, expected %s but received %s.")
    IllegalStateException invalidImplementationLoaded(String type, String expected, String actual);

    @Message(id = 1079, value = "Unable to load module '%s'.")
    RuntimeException unableToLoadModuleRuntime(String module, @Cause Exception cause);

    @Message(id = 1080, value = "Non existing key store needs to have defined type.")
    OperationFailedException nonexistingKeyStoreMissingType();

    @Message(id = 1081, value = "Failed to lazily initialize key manager")
    RuntimeException failedToLazilyInitKeyManager(@Cause  Exception e);

    @Message(id = 1082, value = "Failed to store generated self-signed certificate")
    RuntimeException failedToStoreGeneratedSelfSignedCertificate(@Cause  Exception e);

    @Message(id = 1083, value = "No '%s' found in injected value.")
    RuntimeException noTypeFoundForLazyInitKeyManager(final String type);

    @Message(id = 1084, value = "KeyStore %s not found, it will be auto-generated on first use with a self-signed certificate for host %s")
    @LogMessage(level = WARN)
    void selfSignedCertificateWillBeCreated(String file, String host);

    @Message(id = 1085, value = "Generated self-signed certificate at %s. Please note that self-signed certificates are not secure and should only be used for testing purposes. Do not use this self-signed certificate in production.\nSHA-1 fingerprint of the generated key is %s\nSHA-256 fingerprint of the generated key is %s")
    @LogMessage(level = WARN)
    void selfSignedCertificateHasBeenCreated(String file, String sha1, String sha256);

    @Message(id=1086, value = "Unable to initialize Elytron JACC support while legacy JACC support is enabled.")
    IllegalStateException unableToEnableJaccSupport();

    @Message(id = 1087, value = "Hostname in SNI mapping cannot contain ^ character.")
    OperationFailedException hostContextMapHostnameContainsCaret();

    @Message(id = 1088, value = "Missing certificate authority challenge")
    AcmeException missingCertificateAuthorityChallenge();

    /*
     * Expression Resolver Section
     */

    @Message(id = 1200, value = "The name of the resolver to use was not specified and no default-resolver has been defined.")
    OperationFailedException noResolverSpecifiedAndNoDefault();

    @Message(id = 1201, value = "No expression resolver has been defined with the name '%s'.")
    OperationFailedException noResolverWithSpecifiedName(String name);

    @Message(id = 1202, value = "A cycle has been detected initialising the expression resolver for '%s' and '%s'.")
    ExpressionResolver.ExpressionResolutionUserException cycleDetectedInitialisingExpressionResolver(String firstExpression, String secondExpression);

    @Message(id = 1203, value = "Expression resolver initialisation has already failed.")
    ExpressionResolver.ExpressionResolutionUserException expressionResolverInitialisationAlreadyFailed(@Cause Throwable cause);

    @Message(id = 1204, value = "The expression '%s' does not specify a resolver and no default is defined.")
    ExpressionResolver.ExpressionResolutionUserException expressionResolutionWithoutResolver(String expression);

    @Message(id = 1205, value = "The expression '%s' specifies a resolver configuration which does not exist.")
    ExpressionResolver.ExpressionResolutionUserException invalidResolver(String expression);

    @Message(id = 1206, value = "Unable to decrypt expression '%s'.")
    ExpressionResolver.ExpressionResolutionUserException unableToDecryptExpression(String expression, @Cause Throwable cause);

    @Message(id = 1207, value = "Resolution of credential store expressions is not supported in the MODEL stage of operation execution.")
    ExpressionResolver.ExpressionResolutionServerException modelStageResolutionNotSupported(@Cause IllegalStateException cause);

    @Message(id = 1208, value = "Unable to resolve CredentialStore %s -- %s")
    ExpressionResolver.ExpressionResolutionServerException unableToResolveCredentialStore(String storeName, String details, @Cause Exception cause);

    @Message(id = 1209, value = "Unable to initialize CredentialStore %s -- %s")
    ExpressionResolver.ExpressionResolutionUserException unableToInitializeCredentialStore(String storeName, String details, @Cause Exception cause);

    @Message(id = 1210, value = "Initialisation of an %s without an active management OperationContext is not allowed.")
    ExpressionResolver.ExpressionResolutionServerException illegalNonManagementInitialization(Class<?> initialzingClass);

    @Message(id = 1211, value = "Unable to load the credential store.")
    OperationFailedException unableToLoadCredentialStore(@Cause Throwable cause);

    @Message(id = 1212, value = "KeyStore does not contain a PrivateKey for KeyStore: [%s] and alias: [%s].")
    StartException missingPrivateKey(String keyStore, String alias);

    @Message(id = 1213, value = "KeyStore does not contain a PublicKey for KeyStore: [%s] and alias: [%s].")
    StartException missingPublicKey(String keyStore, String alias);

    @Message(id = 1214, value = "Unable to verify the integrity of the filesystem realm: %s")
    OperationFailedException unableToVerifyIntegrity(@Cause Exception cause, String causeMessage);

    @Message(id = 1215, value = "Filesystem realm is missing key pair configuration, integrity checking is not enabled")
    OperationFailedException filesystemMissingKeypair();

    @Message(id = 1216, value = "Filesystem realm is unable to obtain key store password")
    RuntimeException unableToGetKeyStorePassword();

    @Message(id = 1217, value = "Realm verification failed, invalid signatures for the identities: %s")
    OperationFailedException filesystemIntegrityInvalid(String identities);

    @Message(id = 1218, value = "Keystore used by filesystem realm does not contain the alias: %s")
    KeyStoreException keyStoreMissingAlias(String alias);

    /*
     * Don't just add new errors to the end of the file, there may be an appropriate section above for the resource.
     *
     * If no suitable section is available add a new section.
     */

}
