# Mosaic
Test challenge for Canva (and something extra)


Hi!
I approached your challenge in slightly different way compared to ordinary submission of such challenges. I don't like throwing time away, and whatever I engineer, even if it is a small challenge, I try to build something profound which can be used further. In this particular situation I decided to create a small multimedia framework based on the classic approaches used in the existing systems (DirectShow, OpenMAX, GStreamer, etc.). Yes, for this really easy challenge I created a whole new system! You think overengineering? Or maybe too complex? My way of thinking is: it is better to spend time on something which can be considered as a starting point of next big project. Granted there is normal environment for fixes and tailoring for particular needs. And time. And cookies. 
Regardless of that, I decided that having a streamed mechanism will be suitable for projects in your company, so I called it CanvaProcessor. Based on that, I created a mosaic implementation. Honestly, I'm not happy with the speed of processing. But it's not surprising: implementation is naive Java. I wish I spent more time on writing it in RenderScript or C++ (NDK). Fortunately, I can easily creat such implementations using the existing framework.
Please familiarise yourself with CanvaProcessor documentation (Javadoc in interfaces and base classes).
As for the application, I didn't not focus specifically on the UI, but I wanted to make it neat and simple, almost ready for publishing to Google Play. 
A little excuse me for not clean code everywhere. Especially in processing algorithms. When I get carried away with optimisations I can end up with such horrible lines as index calculation. Of course, if algo goes to more suitable environment (RenderScript, for instance) it will be much neater.
In terms of used libraries. It's really a minimum. I used ButterKnife for UI binding and Universal Image Loader for image loading. That's all. I also used partially FragmentFactory system which I created in one other sample project before. It's not ideal, but simply enough for quick integration. Everything else is written from scratch. Just for you.

