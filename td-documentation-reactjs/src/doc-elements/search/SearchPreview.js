import React, {Component} from 'react'

import elementsLibrary from '../DefaultElementsLibrary'

class SearchPreview extends Component {
    componentDidMount() {
        const Mark = require('mark.js/dist/mark.js') // need to hide from server side rendering
        this.mark = new Mark(this.dom)
    }

    componentDidUpdate() {
        this.highlight()
    }

    render() {
        const {section} = this.props

        const style = {transform: "scale(1.0)"}
        return (<div className="search-result-preview" style={style} ref={(dom) => this.dom = dom}>
            <span>{this.props.snippets}</span>
            <elementsLibrary.Section {...section}/>
        </div>)
    }

    highlight() {
        const {snippets} = this.props
        console.log("snippets", snippets)

        // this.mark.unmark()
        this.mark.mark(snippets)
    }
}

export default SearchPreview