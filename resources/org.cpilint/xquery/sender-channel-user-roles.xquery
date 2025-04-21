(:
    This query returns the name, id, component type and user role of all sender
    channels that are configured with user role authorization.
    
    Please note that since XQuery does not support nested sequences, the returned
    sequence of strings must be processed four elements at a time. If the number of
    returned elements is not either zero or a multiple of four, the result is erroneous.
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
where $direction = "Sender" and
(
($adapterType = ("AS2", "AS4", "HTTPS", "IDOC", "SOAP", "XI") and local:val($mf, "senderAuthType") = "RoleBased")
or
($adapterType = "ODataSender" and local:val($mf, "authentication") = "basic")
)
return (string($mf/@name), string($mf/@id), string($adapterType), local:val($mf, "userRole"))