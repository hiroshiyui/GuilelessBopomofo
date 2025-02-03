#!/usr/bin/env ruby

# Copyright (C) 2025 HUIHONG YOU
#
# This file is part of GuilelessBopomofo.
#
# GuilelessBopomofo is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# GuilelessBopomofo is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with GuilelessBopomofo.  If not, see <https://www.gnu.org/licenses/>.

require "nokogiri"
require "text2svg"
require "victor"
include Victor

# Set default font to Noto Sans CJK TC
FONT = "/usr/share/fonts/opentype/noto/NotoSerifCJK-Regular.ttc"

svg = SVG.new viewBox: "0 0 1024 1024"
keycap_width = 100
keycap_height = 100
padding = 10

CHAR = "ㄟ"
glyph_svg = Nokogiri::XML.parse(Text2svg(CHAR, font: FONT, char_size: "0,10,300,300").to_s.delete("\n")).xpath("//xmlns:g").first.elements.to_s

svg.g id: "keycap", fill: "black" do
  svg.rect x: 0 + padding, y: 0 + padding, width: keycap_width, height: keycap_height, fill: "white", fill_opacity: 0.5
  svg.g id: "glyph" do
    svg.append glyph_svg
  end
end

svg.save "keycaps.svg"
