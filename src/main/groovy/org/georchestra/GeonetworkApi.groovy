package org.georchestra

import groovy.json.JsonSlurper
import groovy.xml.XmlSlurper
import groovy.xml.XmlUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GeonetworkApi {
    private static def payload = """<node id="" type="csw">
  <ownerGroup>
    <id>2</id>
  </ownerGroup>
  <ownerUser>
    <id>undefined</id>
  </ownerUser>
  <site>
    <name></name>
    <rejectDuplicateResource>false</rejectDuplicateResource>
    <capabilitiesUrl></capabilitiesUrl>
    <icon>blank.png</icon>
    <account>
      <use>false</use>
      <username/>
      <password/>
    </account>
    <xpathFilter/>
    <xslfilter/>
    <outputSchema>http://www.isotc211.org/2005/gmd</outputSchema>
    <queryScope>local</queryScope>
    <hopCount>2</hopCount>
  </site>
  <filters/>
  <options>
    <oneRunOnly>false</oneRunOnly>
    <overrideUuid>SKIP</overrideUuid>
    <every></every>
    <status>active</status>
  </options>
  <content>
    <validate>NOVALIDATION</validate>
    <batchEdits/>
  </content>
  <privileges>
    <group id="1">
      <operation name="view"/>
      <operation name="dynamic"/>
      <operation name="download"/>
    </group>
  </privileges>
  <ifRecordExistAppendPrivileges>false</ifRecordExistAppendPrivileges>
  <categories/>
</node>
"""

    private static def geonetworkHarvesterApiUrl  = "https://demo.georchestra.org/geonetwork/srv/eng/admin.harvester.add?_content_type=json"
    private static def geonetworkHarvesterListUrl = "https://demo.georchestra.org/geonetwork/srv/eng/admin.harvester.list?_content_type=json"
    private static def geonetworkHarvesterRunUrl  = "https://demo.georchestra.org/geonetwork/srv/eng/admin.harvester.run?_content_type=json&id="

    private static final Logger logger = LoggerFactory.getLogger(GeonetworkApi.class)

    static def createHarvester(def name, def url) {
        def payload = new XmlSlurper().parseText(payload)
        payload.site.name = name
        payload.site.capabilitiesUrl = url
        payload.options.every = "0 0 12 ${Math.abs(new Random().nextInt() % 28) + 1} * ?"
        payload = XmlUtil.asString(payload)

        def post = new URL(geonetworkHarvesterApiUrl).openConnection()
        post.setRequestMethod("POST")
        post.setDoOutput(true)
        post.setRequestProperty("Content-Type", "application/xml")
        post.setRequestProperty("Accept", "application/json")
        post.setRequestProperty("Authorization", "Basic dGVzdGFkbWluOnRlc3RhZG1pbg==")
        post.setRequestProperty("X-XSRF-TOKEN", "da2f1db8-7f79-4e5a-9da3-2ee47d8dc0b3")
        post.setRequestProperty("Cookie", "XSRF-TOKEN=da2f1db8-7f79-4e5a-9da3-2ee47d8dc0b3")
        post.getOutputStream().write(payload.getBytes("UTF-8"))
        def rc = post.getResponseCode()
        return rc
    }

    static def getHarvesters() {
        def get = new URL(geonetworkHarvesterListUrl).openConnection()
        get.setRequestMethod("GET")
        get.setRequestProperty("Accept", "application/json")
        get.setRequestProperty("Authorization", "Basic dGVzdGFkbWluOnRlc3RhZG1pbg==")
        get.setRequestProperty("X-XSRF-TOKEN", "da2f1db8-7f79-4e5a-9da3-2ee47d8dc0b3")
        get.setRequestProperty("Cookie", "XSRF-TOKEN=da2f1db8-7f79-4e5a-9da3-2ee47d8dc0b3")
        new JsonSlurper().parseText(get.getInputStream().getText())
    }

    static def doHarvest(def hId) {
        def get = new URL("${geonetworkHarvesterRunUrl}${hId}").openConnection()
        get.setRequestMethod("GET")
        get.setRequestProperty("Accept", "application/json")
        get.setRequestProperty("Authorization", "Basic dGVzdGFkbWluOnRlc3RhZG1pbg==")
        get.setRequestProperty("X-XSRF-TOKEN", "da2f1db8-7f79-4e5a-9da3-2ee47d8dc0b3")
        get.setRequestProperty("Cookie", "XSRF-TOKEN=da2f1db8-7f79-4e5a-9da3-2ee47d8dc0b3")
        new JsonSlurper().parseText(get.getInputStream().getText())
    }

    static def isGeonetworkAccessible(def item) {
        def url = new URI(item.site_url.replace("http://", "https://")).resolve("/geonetwork/srv/eng/csw")
        try {
            def c = url.toURL().openConnection()
            c.connect()
            if (c.responseCode != 200) {
                throw new Exception("HTTP code ${c.responseCode}")
            }
        } catch (Exception _) {
            logger.error "{} on {} ", _.getMessage(), url
            return false
        }
        logger.info "Geonetwork found at: {}", url
        item.csw_url = url.toString()
        true
    }

}
