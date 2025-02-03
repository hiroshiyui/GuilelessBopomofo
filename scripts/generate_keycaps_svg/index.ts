// Copyright (C) 2025 HUIHONG YOU
//
// This file is part of keycap_images.
//
// keycap_images is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// keycap_images is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with keycap_images.  If not, see <https://www.gnu.org/licenses/>.

import { createSVGWindow } from 'svgdom'
import { SVG, registerWindow } from '@svgdotjs/svg.js'
import fs from 'node:fs'

// returns a window with a document and an svg root node
const window = createSVGWindow()
const document = window.document

// register window and document
registerWindow(window, document)

// create canvas
const canvas = SVG()

// use svg.js as normal
canvas.rect(100, 100).fill('yellow').move(50, 50)

// get your svg as string
console.log(canvas.svg())

fs.writeFile('./keycaps.svg', canvas.svg(), err => {
    if (err) {
        console.error(err);
    } else {
        // file written successfully
    }
})
