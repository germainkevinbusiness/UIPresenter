# UIPresenter

This library is like a tour guide for your app's UI. It helps you to explain to your user, the role
of the Views in your app's UI.

The library is still under development.

## Here is an example of a Squircle-shaped Presenter

The purple background rectangle with the text is displayed using this library.
<img src="/screenshots/device-2021-12-08-095612.png" alt="A Squircle-shaped Presenter View" width="360" />
<img src="/screenshots/device-2021-12-08-095720.png" alt="A Squircle-shaped Presenter View" width="360" />

## Usage (might change in the future)

This library is only functional when called from a class that is either an Activity or a Fragment.

Basic usage is shown below, when you want to present a View with this library. Inside your Activity,
or Fragment, write this, where you see fit & safe to reference the View you want to present:

```kotlin

UIPresenter(activity = this).set(
    viewToPresent = R.id.the_view_to_present,
    descriptionText = "This is a text explaining what the below list is",
    presenterStateChangeListener = { _, _ -> }
)
```

Or if you just want to specify even more values such as text size, typeface,shadow layer, animation,
animation duration, listening to state changes:

```kotlin
private val purple200 by lazy { ContextCompat.getColor(this, R.color.purple_200) }
private val purple700 by lazy { ContextCompat.getColor(this, R.color.purple_700) }
private val whiteColor by lazy { ContextCompat.getColor(this, R.color.white) }
private val descriptionText = "Proceed to explain what the button's role is"

UIPresenter(fragment = this).set(
    viewToPresent = R.id.the_view_to_present,
    backgroundColor = purple700,
    descriptionText = descriptionText,
    descriptionTextColor = whiteColor,
    revealAnimation = RotationYByAnimation(),
    removeAnimation = FadeOutAnimation(),
    removePresenterOnAnyClickEvent = false,
    shadowLayer = PresenterShadowLayer(shadowColor = purple200, dx = 10f, radius = 10f),
    presenterStateChangeListener = { state, removePresenter ->
        if (state == Presenter.STATE_FOCAL_PRESSED) {
            removePresenter(Unit)
        }
    }
)
```

To create your own animation when the presenter is being added to the decor view
(called reveal animation), you need to extend the ```RevealAnimation``` interface, like so:

````kotlin
class MyRevealAnimation : RevealAnimation {

    override fun runAnimation(
        coroutineScope: CoroutineScope, // A scope to run your animation in, if you want
        presenter: Presenter, // The presenter the reveal animation will run on
        revealAnimationDuration: Long, // The duration of the animation
        afterAnim: () -> Unit // When called that means we can safely consider this animation to be done
    ) {
        // write your animation logic here
    }
}
````

To create your own animation when the presenter is being removed from the decor view
(called remove animation), you need to extend the ```RemoveAnimation``` interface, like so:

````kotlin
class MyRemoveAnimation : RemoveAnimation {

    override fun runAnimation(
        coroutineScope: CoroutineScope, // A scope to run your animation in, if you want
        presenter: Presenter, // The presenter the remove animation will run on
        removeAnimationDuration: Long, // The duration of the animation
        afterAnim: () -> Unit // When called that means we can safely consider this animation to be done
    ) {
        // write your animation logic here
    }
}
````

And then you can apply those animations to your presenter like so:

```kotlin
UIPresenter(activity = this).set(
    revealAnimation = MyRevealAnimation(),
    removeAnimation = MyRemoveAnimation()
)
```

If you want your presenter to be removed from the decor view, on any detected click event, just set
the following parameter in the ````UIPresenter(this).set()```` method to true, like so:

````kotlin
UIPresenter(fragment = this).set(
    removePresenterOnAnyClickEvent = true // This parameter is true by default in the API
)
````

Or if you want to choose when to remove the presenter from the decor view yourself, you can do that
by setting ```removePresenterOnAnyClickEvent = false ``` and listen for state change events, as the
API will give you a ```removePresenter``` function as second parameter in
```presenterStateChangeListener{state,removePresenter -> }```, you can use this function to remove
the presenter whenever you want:

````Kotlin
UIPresenter(fragment = this).set(
    removePresenterOnAnyClickEvent = false, // That'll deactivate automatic removal on any click event
    presenterStateChangeListener = { state, removePresenter ->
        // Here you can choose to remove the presenter whenever you want by calling the
        // removePresenter function like so:
        removePresenter(Unit)
    }
)
````

## License

Licenced under the MIT Licence

```
Copyright (c) 2021 Kevin Germain

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
