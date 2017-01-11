package com.twosigma.documentation.extensions.diagrams.markdown

import com.twosigma.documentation.extensions.diagrams.slides.DiagramSlides
import org.junit.Test

import static com.twosigma.documentation.extensions.diagrams.markdown.MarkdownDiagramSlides.createSlides

/**
 * @author mykola
 */
class MarkdownDiagramSlidesTest {
    private DiagramSlides slides

    static twoSimpleSections = """
# server
server text

# client
client text
"""

    @Test
    void "level 1 header should be treated as id"() {
        parse(twoSimpleSections)

        assert slides.slides.collect { it.ids }.flatten() == ['server', 'client']
    }

    @Test
    void "empty section should be used to define multiple ids"() {
        parse("""
# sub_system_a
# sub_system_b

context information
""")

        assert slides.slides.size() == 1
        assert slides.slides[0].ids == ['sub_system_a', 'sub_system_b']
    }

    @Test
    void "slide content should be doc elements based on markdown"() {
        parse(twoSimpleSections)

        assert slides.slides[0].content*.toMap() == [[type: 'Paragraph', content: [[text: 'server text', type: 'SimpleText']]]]
        assert slides.slides[1].content*.toMap() == [[type: 'Paragraph', content: [[text: 'client text', type: 'SimpleText']]]]
    }

    @Test
    void "slides should be represented as list of maps for client side"() {
        parse(twoSimpleSections)

        assert slides.toListOfMaps() == [[ids: ['server'],
                                          content:[[type: 'Paragraph', content:[[text: 'server text', type: 'SimpleText']]]]],
                                         [ids: ['client'],
                                          content:[[type: 'Paragraph', content:[[text: 'client text', type: 'SimpleText']]]]]]

    }

    private void parse(markdown) {
        slides = createSlides(markdown)
    }
}