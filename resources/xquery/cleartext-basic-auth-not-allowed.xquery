(:
    The query returns the name, id and component type of all receiver channels doing
    basic authentication over unencrypted HTTP (i.e. the username and password
    is sent in cleartext). Channels that go through Cloud Connector are allowed
    to use basic authentication (since the Cloud Connector tunnel is encrypted).
    
    Please note that since XQuery does not support nested sequences, the returned
    sequence of strings must be processed three elements at a time. If the number of
    returned elements is not a multiple of three, the result is erroneous.
    
    What's going on in that where clause? The properties that contain the URL, proxy
    type and authentication method vary by adapter type, sadly. Also, some receiver
    adapters (e.g. AS4) can be configured with multiple endpoints and corresponding
    authentication methods. Each case must be checked individually.
    
    Please note that at this point in time, the AS4 receiver adapter does not support
    Cloud Connector. That's the reason why proxy type is not checked for AS4.
:)

xquery version "3.1";

declare namespace bpmn2 = "http://www.omg.org/spec/BPMN/20100524/MODEL";
declare namespace ifl = "http:///com.sap.ifl.model/Ifl.xsd";

declare function local:val($mf as element(bpmn2:messageFlow), $k as xs:string) as xs:string? {
    $mf/bpmn2:extensionElements/ifl:property[key = $k]/value
};

for $mf in /bpmn2:definitions/bpmn2:collaboration/bpmn2:messageFlow
let $adapterType := local:val($mf, "ComponentType")
let $direction := local:val($mf, "direction")
where $direction = "Receiver" and
(
($adapterType = "HTTP" and starts-with(lower-case(local:val($mf, "httpAddressWithoutQuery")), 'http://') and local:val($mf, "authenticationMethod") = "Basic" and local:val($mf, "proxyType") != "sapcc")
or
($adapterType = "HCIOData" and starts-with(lower-case(local:val($mf, "address")), 'http://') and local:val($mf, "authenticationMethod") = "Basic" and local:val($mf, "proxyType") != "sapcc")
or
($adapterType = "IDOC" and starts-with(lower-case(local:val($mf, "address")), 'http://') and local:val($mf, "authentication") = "Basic" and local:val($mf, "proxyType") != "sapcc")
or
($adapterType = "XI" and starts-with(lower-case(local:val($mf, "Address")), 'http://') and local:val($mf, "AuthenticationType") = "BasicAuthentication" and local:val($mf, "proxyType") != "sapcc")
or
($adapterType = "SOAP" and starts-with(lower-case(local:val($mf, "address")), 'http://') and local:val($mf, "authentication") = "Basic" and local:val($mf, "proxyType") != "sapcc")
or
($adapterType = "AS2" and starts-with(lower-case(local:val($mf, "receipientURL")), 'http://') and local:val($mf, "authenticationType") = "BasicAuthentication" and local:val($mf, "proxyType") != "sapcc")
or
($adapterType = "AS4" and starts-with(lower-case(local:val($mf, "endpointUrl")), 'http://') and local:val($mf, "authenticationType") = "basic")
or
($adapterType = "AS4" and starts-with(lower-case(local:val($mf, "pullReceiptTargetURL")), 'http://') and local:val($mf, "pullReceiptAuthenticationType") = "basic")
)
return (string($mf/@name), string($mf/@id), string($adapterType))