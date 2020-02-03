xquery version "3.1";

declare default element namespace "http://www.w3.org/2005/Atom";
declare namespace m = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
declare namespace d ="http://schemas.microsoft.com/ado/2007/08/dataservices";

for $artifact in /feed/entry
let $iflowId := string($artifact/m:properties/d:Id)
return $iflowId