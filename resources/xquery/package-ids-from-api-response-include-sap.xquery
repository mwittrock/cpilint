(:
    This version of the query only skips read-only packages; it includes
    packages where the vendor is SAP.
:)

xquery version "3.1";

declare default element namespace "http://www.w3.org/2005/Atom";
declare namespace m = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
declare namespace d ="http://schemas.microsoft.com/ado/2007/08/dataservices";

for $package in /feed/entry
let $packageId := string($package/content/m:properties/d:Id)
where $package/content/m:properties/d:Mode = 'EDIT_ALLOWED'
return $packageId