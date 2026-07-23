<?xml version="1.0" encoding="UTF-8"?>
<ctl:package xmlns:ctl="http://www.occamlab.com/ctl"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:tns="http://www.opengis.net/cite/ogcapi-connectedsystems10"
  xmlns:saxon="http://saxon.sf.net/"
  xmlns:tec="java:com.occamlab.te.TECore"
  xmlns:tng="java:org.opengis.cite.ogcapiconnectedsystems10.TestNGController">

  <ctl:function name="tns:run-ets-ogcapi-connectedsystems10">
		<ctl:param name="testRunArgs">A Document node containing test run arguments (as XML properties).</ctl:param>
    <ctl:param name="outputDir">The directory in which the test results will be written.</ctl:param>
		<ctl:return>The test results as a Source object (root node).</ctl:return>
		<ctl:description>Runs the ogcapi-connectedsystems10 ${version} test suite.</ctl:description>
    <ctl:code>
      <xsl:variable name="controller" select="tng:new($outputDir)" />
      <xsl:copy-of select="tng:doTestRun($controller, $testRunArgs)" />
    </ctl:code>
	</ctl:function>

   <ctl:suite name="tns:ets-ogcapi-connectedsystems10-${version}">
     <ctl:title>OGC API - Connected Systems Conformance Test Suite</ctl:title>
     <ctl:description>Tests implemented portions of OGC API - Connected Systems Part 1 (OGC 23-001) and implemented portions of Part 2 (OGC 23-002) using TeamEngine 6.</ctl:description>
     <ctl:starting-test>tns:Main</ctl:starting-test>
   </ctl:suite>
 
   <ctl:test name="tns:Main">
      <ctl:assertion>The test subject satisfies all applicable constraints.</ctl:assertion>
	  <ctl:code>
        <xsl:variable name="form-data">
           <ctl:form method="POST" width="800" height="600" xmlns="http://www.w3.org/1999/xhtml">
             <h2>OGC API - Connected Systems Conformance Test Suite</h2>
             <div style="background:#F0F8FF" bgcolor="#F0F8FF">
               <p>The implementation under test (IUT) is checked against implemented portions of the following specifications:</p>
               <ul>
                 <li><a href="https://docs.ogc.org/is/23-001/23-001.html">OGC API - Connected Systems Part 1: Feature Resources (OGC 23-001)</a></li>
                 <li><a href="https://docs.ogc.org/is/23-002/23-002.html">OGC API - Connected Systems Part 2: Dynamic Data (OGC 23-002)</a></li>
               </ul>
               <p>TeamEngine 6 is the forward runtime for this ETS. Local OSH is the primary development E2E target; GeoRobotix is an advisory interoperability target only.</p>
             </div>
             <fieldset style="background:#ccffff">
               <legend style="font-family: sans-serif; color: #000099; 
			                 background-color:#F0F8FF; border-style: solid; 
                       border-width: medium; padding:4px">Implementation under test</legend>
               <p>
                 <label for="iut">
                   <h4 style="margin-bottom: 0.5em">CS API landing page URL</h4>
                 </label>
                 <input id="iut" name="iut" size="128" type="text" value="" />
               </p>
               <p>
                 <label for="auth-credential">
                   <h4 style="margin-bottom: 0.5em">Authorization header value</h4>
                 </label>
                 <input id="auth-credential" name="auth-credential" size="128" type="password" value="" />
               </p>
               <p>
                 <input id="mutation-tests-enabled" name="mutation-tests-enabled" type="checkbox" value="true" />
                 <label for="mutation-tests-enabled"> Enable create/replace/delete mutation tests</label>
               </p>
               <p>
                 <label for="mutation-iut-policy">
                   <h4 style="margin-bottom: 0.5em">Mutation IUT policy</h4>
                 </label>
                 <input id="mutation-iut-policy" name="mutation-iut-policy" size="48" type="text" value="" />
               </p>
             </fieldset>
             <p>
               <input class="form-button" type="submit" value="Start"/> | 
               <input class="form-button" type="reset" value="Clear"/>
             </p>
           </ctl:form>
        </xsl:variable>
	      <xsl:variable name="test-run-props">
		    <properties version="1.0">
          <entry key="iut">
            <xsl:value-of select="normalize-space($form-data/values/value[@key='iut'])"/>
          </entry>
          <entry key="auth-credential"><xsl:value-of select="normalize-space($form-data/values/value[@key='auth-credential'])"/></entry>
          <entry key="mutation-tests-enabled"><xsl:value-of select="normalize-space($form-data/values/value[@key='mutation-tests-enabled'])"/></entry>
          <entry key="mutation-iut-policy"><xsl:value-of select="normalize-space($form-data/values/value[@key='mutation-iut-policy'])"/></entry>
		    </properties>
		   </xsl:variable>
       <xsl:variable name="testRunDir">
         <xsl:value-of select="tec:getTestRunDirectory($te:core)"/>
       </xsl:variable>
       <xsl:variable name="test-results">
        <ctl:call-function name="tns:run-ets-ogcapi-connectedsystems10">
			    <ctl:with-param name="testRunArgs" select="$test-run-props"/>
          <ctl:with-param name="outputDir" select="$testRunDir" />
			  </ctl:call-function>
		  </xsl:variable>
      <xsl:call-template name="tns:testng-report">
        <xsl:with-param name="results" select="$test-results" />
        <xsl:with-param name="outputDir" select="$testRunDir" />
      </xsl:call-template>
      <xsl:variable name="summary-xsl" select="tec:findXMLResource($te:core, '/testng-summary.xsl')" />
      <ctl:message>
        <xsl:value-of select="saxon:transform(saxon:compile-stylesheet($summary-xsl), $test-results)"/>
See detailed test report in the TE_BASE/users/<xsl:value-of 
select="concat(substring-after($testRunDir, 'users/'), '/html/')" /> directory.
        </ctl:message>
        <xsl:if test="xs:integer($test-results/testng-results/@failed) gt 0">
          <xsl:for-each select="$test-results//test-method[@status='FAIL' and not(@is-config='true')]">
            <ctl:message>
Test method <xsl:value-of select="./@name"/>: <xsl:value-of select=".//message"/>
		    </ctl:message>
		  </xsl:for-each>
		  <ctl:fail/>
        </xsl:if>
        <xsl:if test="xs:integer($test-results/testng-results/@skipped) eq xs:integer($test-results/testng-results/@total)">
        <ctl:message>All tests were skipped. One or more preconditions were not satisfied.</ctl:message>
        <xsl:for-each select="$test-results//test-method[@status='FAIL' and @is-config='true']">
          <ctl:message>
            <xsl:value-of select="./@name"/>: <xsl:value-of select=".//message"/>
          </ctl:message>
        </xsl:for-each>
        <ctl:skipped />
      </xsl:if>
	  </ctl:code>
   </ctl:test>

  <xsl:template name="tns:testng-report">
    <xsl:param name="results" />
    <xsl:param name="outputDir" />
    <xsl:variable name="stylesheet" select="tec:findXMLResource($te:core, '/testng-report.xsl')" />
    <xsl:variable name="reporter" select="saxon:compile-stylesheet($stylesheet)" />
    <xsl:variable name="report-params" as="node()*">
      <xsl:element name="testNgXslt.outputDir">
        <xsl:value-of select="concat($outputDir, '/html')" />
      </xsl:element>
    </xsl:variable>
    <xsl:copy-of select="saxon:transform($reporter, $results, $report-params)" />
  </xsl:template>
</ctl:package>
