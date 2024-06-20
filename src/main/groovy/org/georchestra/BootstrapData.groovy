package org.georchestra

import org.jsoup.Jsoup
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BootstrapData {

    private static def websiteSourceUrl = "https://raw.githubusercontent.com/georchestra/georchestra.github.io/master/community.md"
    private static def instancesUrl = "https://demo.georchestra.org/geoserver/ows?service=wfs&request=GetFeature&outputFormat=application/json&typeName=georchestra_instances"

    private static final Logger logger = LoggerFactory.getLogger(BootstrapData.class)


    static def instanceFromWebsiteSources() {
        def content = new URL(websiteSourceUrl).readLines()
        content.findAll {
            it.startsWith("  {% include card_user.html ")
        }.collect {
            def frag = it.replace("{% include card_user.html ", "<span ").replace(" %}", " />")
            def lump = Jsoup.parse(frag)

            [
                    'img_url': lump.selectFirst("span").attr("img_url"),
                    'name': lump.selectFirst("span").attr("name"),
                    'site_url': lump.selectFirst("span").attr("site_url")
            ]
        }

    }

    public static void main(String[] args) {
        def gnApi = new GeonetworkApi()

        def instances = instanceFromWebsiteSources().findAll {
            gnApi.isGeonetworkAccessible(it)
        }
        def harvesters = gnApi.getHarvesters()
        def harvestersNames = harvesters.collect { it.site.name }
        def harvestersCreated = []

        // creating harvesters endpoints in GN (if they don't exist yet)

        instances.each {
            def instanceName = it.name
            if (instanceName !in harvestersNames) {
                gnApi.createHarvester(it.name, it.csw_url)
                harvestersCreated << it.name
            } else {
                logger.info "Harvester endpoint '{}' already configured", it.name
            }
        }
        // Adding craig
        if ("CRAIG" !in harvestersNames) {
            gnApi.createHarvester("CRAIG", "https://ids.craig.fr/geocat/srv/eng/csw")
            harvestersCreated << "CRAIG"
        } else {
            logger.info "Harvester endpoint 'CRAIG' already configured"
        }

        // launches the created harvesters (requires to refresh the list from GeoNetwork)
        harvesters = gnApi.getHarvesters()

        harvestersCreated.each { h -> {
                def hId = harvesters.find { it.site.name == h }?.'@id'
                logger.info "Launching newly created harvester '{}' ...", h
                gnApi.doHarvest(hId)
            }
        }

        System.exit(0)
    }
}
