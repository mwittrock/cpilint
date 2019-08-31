(:
    This query returns the name, id and address of all receiver ProcessDirect channels.
    Note that since XQuery does not support nested sequences, the returned sequence
    of strings must be processed three elements at a time. If the number of returned
    elements is not a multiple of three, the result is erroneous.
:)

xquery version "3.1";

declare namespace bpmn2 = "http://www.omg.org/spec/BPMN/20100524/MODEL";
declare namespace ifl = "http:///com.sap.ifl.model/Ifl.xsd";

for $c in //bpmn2:messageFlow
where $c/bpmn2:extensionElements/ifl:property[key = 'direction']/value = 'Receiver'
and   $c/bpmn2:extensionElements/ifl:property[key = 'ComponentType']/value = 'ProcessDirect'
return (string($c/@name), string($c/@id), string($c/bpmn2:extensionElements/ifl:property[key = 'address']/value))