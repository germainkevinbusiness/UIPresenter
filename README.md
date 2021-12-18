# UIPresenter

[![](https://jitpack.io/v/germainkevinbusiness/UIPresenter.svg)](https://jitpack.io/#germainkevinbusiness/UIPresenter)

This android library is like a tour guide for your app's Views. It helps to explain to your user,
the role of the Views in your Activity or Fragment. It supports ```minSdk 21``` and up.

<img src="/screenshots/device-2021-12-17-140025.png" alt="UIPresenter example 2 screenshot"/>

<div>
<img src="/screenshots/device-2021-12-17-135021.png" alt="UIPresenter example 2 screenshot" width="360" />
<img src="/screenshots/device-2021-12-17-135123.png" alt="A Menu item being presented by the library" width="360" />
</div>

# How to get this library in your android app

**Step 1.** Add the jitpack repository to the ``repositories { }``  function, inside
your ``project build.gradle`` or your ``settings.gradle`` like so:

```groovy
repositories {
    google()
    mavenCentral()
    // Place the jitpack repository inside this, like so:
    maven { url 'https://jitpack.io' }
}
```

**Step 2.** Add the dependency in your ``` module build.gradle ``` file, like so:

```groovy
dependencies {
    implementation 'com.github.germainkevinbusiness:UIPresenter:1.0.0-beta12'
}
```

**That's it!**

## Usage

This library is only functional when called from a class that is either an Activity or a Fragment.

Basic usage is shown below, with more examples in the
[sample app](https://github.com/germainkevinbusiness/UIPresenter/tree/master/sample).

```kotlin

UIPresenter(activity = this).set(
    viewToPresent = binding.recyclerView[2], // using view binding here
    descriptionText = "This is a row inside the RecyclerView with an animal image and name",
    presenterStateChangeListener = { _, _ -> }
)
```

Only the above three parameters inside the ```UIPresenter.set()``` method are required to display
a ```Presenter``` on your ```Activity``` or ```Fragment```'s UI. The rest of the parameters in
the ```UIPresenter.set()``` method are optional, but great for customization!

### Here's the output of the above code in light and dark mode:

<div>
<img src="/screenshots/device-2021-12-15-181310.png" alt="Light mode UIPresenter example" width="360" />
<img src="/screenshots/device-2021-12-15-181210.png" alt="Dark mode UIPresenter example" width="360" />
</div>

Or if you want to specify even more values while listening to state changes:

```kotlin
private val teal200 = ContextCompat.getColor(this, R.color.teal200)
private val purple700 = ContextCompat.getColor(this, R.color.purple700)
private val whiteColor = ContextCompat.getColor(this, R.color.whiteColor)

// Or for a Menu item in your toolbar
private fun presentMenuItem() {
    UIPresenter(fragment = this).set(
        viewToPresentId = R.id.action_present_view,
        backgroundColor = purple700,
        descriptionText = getString(R.string.menu_play_desc),
        descriptionTextColor = whiteColor,
        presenterShape = SquircleShape(),
        revealAnimation = RotationYByAnimation(),
        removeAnimation = FadeOutAnimation(),
        revealAnimDuration = 600L,
        removalAnimDuration = 600L,
        descriptionTextTypeface = Typeface.MONOSPACE,
        presenterHasShadowedWindow = true,
        removePresenterOnAnyClickEvent = false,
        removeOnBackPress = true,
        shadowLayer = PresenterShadowLayer(shadowColor = blue500),
        presenterStateChangeListener = { state, removePresenter ->
            if (state == Presenter.STATE_FOCAL_PRESSED) {
                removePresenter()
            }
        }
    )
}
```

If you want to remove the Presenter on certain specific click events, here are the click events that
the UIPresenter library can detect:

```kotlin
// When a click is done on the view you want to present
Presenter.STATE_VTP_PRESSED
// When a click is done on the presenter's PresenterShape which is the presenter's visible part
// with the description text, background & shadow layer
Presenter.STATE_FOCAL_PRESSED
// When a click is done outside the presenter's PresenterShape 
// and outside the view you want to present
Presenter.STATE_NON_FOCAL_PRESSED
// When a press on the back button is detected
Presenter.STATE_BACK_BUTTON_PRESSED

UIPresenter(fragment = this).set(
    // Now the library won't removes the presenter on any detected click event automatically
    // You now have to decide which click event will remove the presenter by yourself, like
    // show inside the presenterStateChangeListener below
    removePresenterOnAnyClickEvent = false,
    presenterStateChangeListener = { state, removePresenter ->
        // Removes the presenter when a click is done on the presenter's PresenterShape 
        // which is the presenter's visible part with the description text, 
        // background & shadow layer
        if (state == Presenter.STATE_FOCAL_PRESSED) {
            // Removes the presenter
            removePresenter()
        }
    }
)
```

You can apply your own animations to the presenter like so:

```kotlin
UIPresenter(activity = this).set(
    revealAnimation = MyRevealAnimation(),
    removeAnimation = MyRemoveAnimation()
)
```

To create your own animation when the presenter is being added to the decor view
(called reveal animation), you need to implement the ```RevealAnimation``` interface, like so:

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
(called remove animation), you need to implement the ```RemoveAnimation``` interface, like so:

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
