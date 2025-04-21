(:
    This query returns a sequence of strings, containing the addresses of all
    sender ProcessDirect channels.
:)

xquery version "3.1";

declare namespace bpmn2 = "http://www.omg.org/spec/BPMN/20100524/MODEL";
declare namespace ifl = "http:///com.sap.ifl.model/Ifl.xsd";

for $c in //bpmn2:messageFlow
where $c/bpmn2:extensionElements/ifl:property[key = 'direction']/value = 'Sender'
and   $c/bpmn2:extensionElements/ifl:property[key = 'ComponentType']/value = 'ProcessDirect'
return string($c/bpmn2:extensionElements/ifl:property[key = 'address']/value)