from pathlib import Path
from PIL import Image, ImageDraw

out = Path(__file__).parents[1] / "src/main/resources/assets/true_eternal_friend/textures/item/eternal_friend_tag.png"
out.parent.mkdir(parents=True, exist_ok=True)
im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
d = ImageDraw.Draw(im)
outline=(48,42,40,255); iron=(205,174,122,255); shade=(149,116,77,255)
white=(226,226,216,255); grey=(166,171,169,255); dark=(45,45,45,255)
brown=(101,61,38,255); red=(177,45,50,255); gold=(235,180,55,255)
d.point((7,0), fill=outline); d.rectangle((6,1,9,2), fill=outline); d.point((7,1), fill=iron)
d.rectangle((3,3,12,13), fill=outline); d.rectangle((4,4,11,12), fill=iron); d.rectangle((5,5,10,11), fill=(220,190,140,255))
d.rectangle((5,5,6,7), fill=grey); d.point((5,5), fill=dark); d.rectangle((9,5,10,7), fill=grey); d.point((10,5), fill=dark)
d.rectangle((5,7,10,10), fill=white); d.point((6,8), fill=brown); d.point((9,8), fill=brown)
d.rectangle((7,9,8,10), fill=dark); d.point((7,10), fill=dark); d.rectangle((5,10,10,11), fill=red); d.rectangle((7,11,8,12), fill=gold)
d.point((4,4), fill=shade); d.point((11,4), fill=shade); d.point((4,12), fill=shade); d.point((11,12), fill=shade)
im.save(out)
assert im.size == (16,16) and im.mode == "RGBA"
icon = out.parents[2] / "icon.png"
im.resize((64,64), Image.Resampling.NEAREST).save(icon)
print(out, im.size, im.mode)
