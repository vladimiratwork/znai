/*
 * Copyright 2020 znai maintainers
 * Copyright 2019 TWO SIGMA OPEN SOURCE, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React from 'react'
import EmbeddedAnnotatedImage from './EmbeddedAnnotatedImage'

export function imageDemo(registry) {
    registry.add('simple annotations', () => <EmbeddedAnnotatedImage {...simpleAnnotations()}/>)
    registry.add('left aligned', () => <EmbeddedAnnotatedImage {...leftAlign()}/>)
    registry.add('right aligned', () => <EmbeddedAnnotatedImage {...rightAlign()}/>)
}

function simpleAnnotations() {
    return {
        imageSrc: 'ui.jpg',
        width: 800,
        height: 500,
        shapes: [
            {type: 'circle', id: 'c1', x: 100, y: 100, r: 20, text: '1'},
            {type: 'circle', id: 'c1', x: 500, y: 100, r: 20, text: '1', color: 'a'},
            {type: 'circle', id: 'c2', x: 180, y: 100, r: 20, text: '2', color: 'b'},
            {type: 'circle', id: 'c3', x: 150, y: 150, r: 30, text: '3', color: 'c'},
            {type: 'highlight', id: 'h1', x: 150, y: 220, width: 80, height: 40, color: 'c'},
            {type: 'rectangle', id: 'c3', x: 270, y: 170, width: 80, height: 40, text: 'here', color: 'b'},
            {type: 'arrow', id: 'a1', beginX: 200, beginY: 200, endX: 300, endY: 300, color: 'd', text: 'This here'}]
    }
}

function leftAlign() {
    return {
        imageSrc: 'ui.jpg',
        align: 'left',
        width: 400,
        height: 250,
    }
}

function rightAlign() {
    return {
        imageSrc: 'ui.jpg',
        align: 'right',
        width: 400,
        height: 250,
    }
}
