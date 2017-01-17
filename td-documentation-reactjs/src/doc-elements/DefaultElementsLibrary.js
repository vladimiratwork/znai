import React from 'react'
import DocElement from './DocElement'
import Snippet from './Snippet'
import GraphVizSvg from './graphviz/GraphVizSvg'

const library = {}

const BoundDocElement = ({content}) => <DocElement content={content} elementsLibrary={library} />

library.DocElement = BoundDocElement
library.Emphasis = ({content}) => (<span className="emphasis"><BoundDocElement content={content} /></span>)
library.StrongEmphasis = ({content}) => (<span className="strong-emphasis"><BoundDocElement content={content} /></span>)
library.Link = ({anchor, label}) => (<a href={anchor}>{label}</a>)
library.Paragraph = ({content}) => <div className="paragraph content-block"><BoundDocElement content={content} /></div>
library.BlockQuote = ({content}) => <blockquote className="content-block"><BoundDocElement content={content} /></blockquote>
library.SimpleText = ({text}) => <span className="simple-text">{text}</span>
library.SoftLineBreak = () => <span> </span>
library.HardLineBreak = () => <br />
library.ThematicBreak = () => <hr />

library.Snippet = Snippet

library.BulletList = ({tight, bulletMarker, content}) => {
    const className = "content-block" + (tight ? " tight" : "")
    return (<ul className={className}><BoundDocElement content={content} /></ul>)
}

library.OrderedList = ({delimiter, startNumber, content}) => <ol className="content-block" start={startNumber}><BoundDocElement content={content} /></ol>

library.ListItem = ({content}) => <li><BoundDocElement content={content} /></li>

library.Section = ({title, content}) => (
    <div className="section">
        <div className="section-title">{title}</div>
        <BoundDocElement content={content} />
    </div>)

library.CustomComponent = ({componentName, componentProps}) => {
    const Component = library[componentName]
    if (!Component) {
        console.warn("can't find a custom component: ", componentName)
        return null
    } else {
        return <Component {...componentProps} />
    }
}

library.FileTextContent = (props) => <Snippet snippet={props.text} {...props}/>
library.GraphVizSvg = GraphVizSvg

library.Page = ({title, content}) => (<div className="page-content">
    <BoundDocElement content={content}/>
</div>)

export default library
