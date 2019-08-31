xquery version "3.1";

declare namespace bpmn2 = "http://www.omg.org/spec/BPMN/20100524/MODEL";
declare namespace ifl = "http:///com.sap.ifl.model/Ifl.xsd";

for $gateway in //bpmn2:exclusiveGateway[bpmn2:extensionElements/ifl:property[key = 'activityType']/value = 'ExclusiveGateway']
let $nonDefaultRoutes := //bpmn2:sequenceFlow[@sourceRef = $gateway/@id][@id != $gateway/@default]
let $distrinctExpressionTypes := count(distinct-values($nonDefaultRoutes/bpmn2:extensionElements/ifl:property[key = 'expressionType']/value))
where $distrinctExpressionTypes > 1
return $gateway