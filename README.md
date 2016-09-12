# Mosaic
##Test challenge for Canva (and something extra)


Hi!

I approached the challenge in slightly different way compared to ordinary way of making such things. I could create some functions and random encapsulating entities and nail this test in few hours. The thing is, I don't like throwing time away, and whatever I engineer, even if it is a small challenge, I try to build something profound which can be used later. In this particular situation I decided to create a small multimedia framework based on the classic approaches used in the existing systems (DirectShow, OpenMAX, GStreamer, etc.). Why? Simply because I always wanted to do it and now I've got a suitable opportunity. So, yes, for this little challenge I created a whole new system! You think overengineering? Or maybe too complex? My way of thinking is: it is better to spend time on something which can be considered as a starting point of next big project. Granted there is good environment for tailoring the solution for particular needs. And time. And cookies. 

Regardless of that, I decided that having sort of a modular streaming mechanism will be suitable for projects in your company, so I called it CanvaProcessor. And I liked it. Based on that, I created a mosaic implementation. But honestly, I'm not happy with the speed of processing. It's not surprising, though: the implementation is naive Java. I wish I spent more time on writing it in RenderScript or C++ (NDK). Fortunately, I can easily create such implementations using the newly crated framework (it's my next turn, actually! see below...).

Please familiarise yourself with **CanvaProcessor** documentation (see Javadoc in interfaces and base classes).

As for the application, I didn't not focus specifically on the UI, but I wanted to make it neat and simple, almost ready for publishing to Google Play. 

**In terms of used libraries**. It's really a minimum. I used ButterKnife for UI binding and Universal Image Loader for image loading. That's all. I also used partially FragmentFactory code which I created in another sample project before. It's not ideal, but simple enough for quick integration. Everything else is written from scratch. Just for you.

##Good things to expect
- You can actually use the app to create mosaic and share it. 
- You can play with different tile sizes. 
- And you can pick any image you want (but the bigger it is, the longer it will take it to render, alas). 
- The app looks nice, I tried to make it looking like a real app, not a silly prototype.
- More important, you can take CanvaProcessor and start using it in real applications. It needs some love, of course, as it's brand new.

##Bad things to expect
- Yep, it's not as fast as I could make it. I wanted initially to write processing in C++, but decided to focus more on CanvaProcessor, leaving not enough time to optimise the mosaic transformation.
- Honestly, I did not try it on other devices. I strongly believe it would work as the UI is simplistic and CanvaProcessor is device-agnostic.
- Finally afew know bugs. You might eventually run into stability issues such as out of memory crash (mem usage to be optimised and leaks fixed) or heavy garbage collection slowing down the app. There is another bug when updating the API endpoint IP, you need to reload image for new address to take effect.

##What I really want to do next
1. Hardcore optimisation of algorithms: RenderScript or NDK. 
2. Work on CanvaProcessor to make it more robust and versatile (things like caching, data allocations, cancellation and thread management)
3. Reduce pixel count in the original image (say, to 5 bit per channel) for even faster processing
4. Write couple of other awesome effects and include them in app for users to choose and combine.
5. Undo/Redo


##Finally
I hope that you will understand my motivation. I'll recap: I designed the app this wat not only because I want to join your company, but also because engineering awesome robust solutions is in my blood. I really wish you enjoy it and I'm looking forward to meeting you soon!


PS. A little excuse me for not clean code everywhere. Especially in processing algorithms. When I get carried away with optimisations I can end up with such horrible lines as index calculation. Of course, if algo goes to more suitable environment (RenderScript, for instance) it will be much neater.

####Spent time break-down
4h on CanvaProcessor proto
4h on App UI
8h on processes, networking, business logic
3h polishing, bugfixing
2h documentation and readme

