(:
    The query always skips read-only packages. The external variable
    $skipSapPackages, which is set from Java, determines whether SAP
    packages (i.e. packages where the vendor is SAP) are also skipped.
    The default behaviour is to not skip them.
:)

xquery version "3.1";

declare default element namespace "http://www.w3.org/2005/Atom";
declare namespace m = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
declare namespace d = "http://schemas.microsoft.com/ado/2007/08/dataservices";

declare variable $skipSapPackages as xs:boolean external := fn:false();

if ($skipSapPackages) then (
    for $package in /feed/entry
    let $packageId := string($package/m:properties/d:Id)
    where $package/m:properties/d:Mode = 'EDIT_ALLOWED' and $package/m:properties/d:Vendor != 'SAP'
    return $packageId
) else (
    for $package in /feed/entry
    let $packageId := string($package/m:properties/d:Id)
    where $package/m:properties/d:Mode = 'EDIT_ALLOWED'
    return $packageId
)

