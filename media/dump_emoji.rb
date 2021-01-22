require "unicode/emoji"

emoji_ordering = File.read("./emoji-ordering.txt")

File.open("./emoji_dump.txt", 'w+') do |f|
  f.puts(emoji_ordering.scan(Unicode::Emoji::REGEX))
end
