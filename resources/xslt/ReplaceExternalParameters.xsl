<?xml version="1.0" encoding="UTF-8"?>

<!--
    This stylesheet performs an identity transformation, with the exception
    that external parameters ({{MY_PARAM_NAME}}) are replaced with their
    configured values within bpmn2:extensionElements/ifl:property/value
    elements. A map of parameter names and values is provided by the calling
    application.
 -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:cpilint="http://mwittrock.dk/cpilint"
    xmlns:map="http://www.w3.org/2005/xpath-functions/map"
    xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL"
    xmlns:ifl="http:///com.sap.ifl.model/Ifl.xsd"
    exclude-result-prefixes="xs cpilint map bpmn2 ifl"
    version="3.0">

    <xsl:mode on-no-match="shallow-copy" />

    <xsl:param name="parameterMap" as="map(xs:string, xs:string)" required="yes"/>
    
    <xsl:template match="bpmn2:extensionElements/ifl:property/value">
        <value>
            <xsl:value-of select="cpilint:replaceExternalParams(string(.))"/>
        </value>
    </xsl:template>
    
    <xsl:function name="cpilint:replaceExternalParams">
        <xsl:param name="withinText" as="xs:string"/>
        <!--
            Why the \{{ and \}} for curly brackets? The curly bracket must be doubled, in order
            for the processor to understand that it's not the start of an attribute value template.
            Next, it must be quoted, since the curly bracket is a special character in regular 
            expressions. As for matching parameter names, the following was copied from the
            CPI developer's guide:
            "You can use alphanumeric characters, underscore and hyphen for parameter name."
        -->
        <xsl:analyze-string select="$withinText" regex="\{{\{{([-_a-zA-Z0-9]+)\}}\}}">
            <xsl:matching-substring>
                <xsl:choose>
                    <xsl:when test="map:contains($parameterMap, regex-group(1))">
                        <!-- This is a known parameter, replace it with its value. -->
                        <xsl:value-of select="$parameterMap(regex-group(1))"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- This is an unknown parameter, leave it in place. -->
                        <xsl:value-of select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:matching-substring>
            <xsl:non-matching-substring>
                <xsl:value-of select="."/>
            </xsl:non-matching-substring>    
        </xsl:analyze-string>
    </xsl:function>
    
</xsl:stylesheet>