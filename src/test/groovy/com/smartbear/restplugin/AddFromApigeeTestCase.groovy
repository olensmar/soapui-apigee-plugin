package com.smartbear.restplugin

class AddFromApigeeTestCase extends GroovyTestCase {
    void testGetConsoles() {
        def action = new AddAPIFromApigeeAction()
        def entries = action.initEntries("file:src/test/resources/consoles.json")

        assertFalse(entries.isEmpty())

        entries.values().each {
            it
            assertTrue(it.wadlUrl.endsWith("?format=wadl"))
        }
    }

}
