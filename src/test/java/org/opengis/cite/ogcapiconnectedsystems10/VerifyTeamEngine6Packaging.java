package org.opengis.cite.ogcapiconnectedsystems10;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.BeforeClass;
import org.junit.Test;

/** Structural regression checks for the TeamEngine 6 packaging boundary. */
public class VerifyTeamEngine6Packaging {

	private static String dockerfile;

	private static String compose;

	private static String pom;

	@BeforeClass
	public static void loadPackagingFiles() throws IOException {
		dockerfile = Files.readString(Path.of("Dockerfile"));
		compose = Files.readString(Path.of("docker-compose.yml"));
		pom = Files.readString(Path.of("pom.xml"));
	}

	/**
	 * REQ-ETS-TEAMENGINE-007; SCENARIO-ETS-TEAMENGINE-TE6-IMAGE-PROVENANCE-001.
	 */
	@Test
	public void runtimeUsesPinnedTeamEngine6Image() {
		assertTrue(dockerfile.contains(
				"FROM ogccite/teamengine-dev@sha256:981b71566d56434576843798ae8072db15be8478eb7dc724b051c2228460f43c"));
		assertTrue(pom.contains("<teamengine.runtime.version>6.0.0</teamengine.runtime.version>"));
		assertTrue(pom.contains(
				"<teamengine.runtime.image.digest>sha256:981b71566d56434576843798ae8072db15be8478eb7dc724b051c2228460f43c</teamengine.runtime.image.digest>"));
	}

	/**
	 * REQ-ETS-TEAMENGINE-003, REQ-ETS-TEAMENGINE-007;
	 * SCENARIO-ETS-TEAMENGINE-TE6-BASE-IMMUTABILITY-001.
	 */
	@Test
	public void runtimeDoesNotModifyTeamEngineOwnedFiles() {
		String logicalDockerfile = dockerfile.replace("\\\n", " ");
		assertFalse(dockerfile.contains("USER root"));
		assertFalse(logicalDockerfile.matches(
				"(?s).*(?m:^RUN\\s+[^\\r\\n]*(?:sed|chmod|chown|rm)\\s+[^\\r\\n]*(?:webapps/teamengine|te_base)[^\\r\\n]*$).*"));
		assertFalse(dockerfile.contains("chown -R"));
		assertFalse(dockerfile.contains("teamengine-*.jar"));
		assertTrue(dockerfile.contains("USER tomcat"));
	}

	/**
	 * REQ-ETS-TEAMENGINE-003; SCENARIO-ETS-TEAMENGINE-TE6-DEPENDENCY-INVENTORY-001.
	 */
	@Test
	public void dependencyExclusionsAreExplicit() {
		assertFalse(dockerfile.contains("rm -f target/lib-runtime/teamengine-*.jar"));
		assertFalse(dockerfile.contains("/build/target/lib-runtime/ /usr/local/tomcat"));
		assertTrue(dockerfile.contains("target/lib-runtime-selected/"));
		assertTrue(dockerfile.contains("org.opengis.cite.teamengine:teamengine-resources:6.0.0"));
		assertTrue(pom.contains("<artifactId>maven-shade-plugin</artifactId>"));
		assertTrue(pom.contains("org.opengis.cite.ogcapiconnectedsystems10.internal.networknt.schema"));
		assertFalse(dockerfile.contains("json-schema-validator-1.5.9.jar"));
		assertFalse(dockerfile.contains("guava-31.0.1-jre.jar"));
		assertFalse(pom.contains("dependency/*teamengine-*.war"));
		assertFalse(pom.contains("<artifactId>teamengine-web</artifactId>"));
		assertFalse(pom.contains("<artifactId>teamengine-console</artifactId>"));
	}

	/**
	 * REQ-ETS-TEAMENGINE-004, REQ-ETS-TEAMENGINE-007;
	 * SCENARIO-ETS-TEAMENGINE-TE6-CONFIG-ALIGNMENT-001.
	 */
	@Test
	public void composeInheritsRuntimeConfiguration() {
		assertFalse(compose.contains("JAVA_OPTS:"));
		assertFalse(compose.contains("CATALINA_OPTS:"));
		assertTrue(compose.contains("8081:8080"));
		assertTrue(compose.contains("http://localhost:8080/teamengine/"));
	}

	/**
	 * REQ-ETS-TEAMENGINE-007; SCENARIO-ETS-TEAMENGINE-TE6-BASE-IMMUTABILITY-001.
	 */
	@Test
	public void ctlInstallationRejectsBasePathCollisions() {
		assertTrue(dockerfile.contains("test ! -e /usr/local/tomcat/te_base/scripts/ogcapi-connectedsystems10"));
		assertFalse(dockerfile.contains("unzip -q -o"));
		assertTrue(dockerfile.contains("test -d /tmp/ets-ctl/ogcapi-connectedsystems10"));
	}

	/**
	 * REQ-ETS-CLEANUP-021; SCENARIO-ETS-CLEANUP-CONFIDENTIAL-BUILD-CONTEXT-001.
	 */
	@Test
	public void dockerfileProvidesEffectiveContextAuditStage() {
		assertTrue(dockerfile.contains("AS build-context-audit"));
		assertTrue(dockerfile.contains("COPY . /context"));
	}

}
