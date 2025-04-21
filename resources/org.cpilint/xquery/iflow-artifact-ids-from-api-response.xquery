(:
    The external variable $skipDrafts, which is set from Java, determines
    whether draft iflows are skipped. The default behaviour is to not
    skip them.
:)

xquery version "3.1";

declare default element namespace "http://www.w3.org/2005/Atom";
declare namespace m = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
declare namespace d ="http://schemas.microsoft.com/ado/2007/08/dataservices";

declare variable $skipDrafts as xs:boolean external := fn:false();

if ($skipDrafts) then (
    for $artifact in /feed/entry
    let $iflowId := string($artifact/m:properties/d:Id)
    where $artifact/m:properties/d:Version != 'Active'
    return $iflowId
) else (
    for $artifact in /feed/entry
    let $iflowId := string($artifact/m:properties/d:Id)
    return $iflowId
)