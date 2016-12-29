package com.twosigma.utils

import org.junit.Test

/**
 * @author mykola
 */
class ResourceUtilsTest {
    @Test
    void "should read text from a single resource file"() {
        def content = ResourceUtils.textContent("single.txt")
        assert content == "single resource\nfile"
    }

    @Test(expected = IllegalArgumentException)
    void "should validate single resource presence"() {
        ResourceUtils.textContent("not-found.txt")
    }

    @Test
    void "should read texts from multiple resource files with the same name"() {
        def contents = ResourceUtils.textContents("important/meta.txt")

        assert contents.size() == 2
        contents.any { it == "second hello meta\nsecond txt" }
        contents.any { it == "hello meta\ntxt" }
    }
}
