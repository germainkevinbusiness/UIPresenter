# UIPresenter

This android library is like a tour guide for your app's Views. It helps to explain to your user,
the role of the Views in your Activity or Fragment.

The library is still under development.

## Here is an example of a Squircle-shaped Presenter

The layer on top of the below app is displayed using this library.

<div>
<img src="/screenshots/device-2021-12-15-175804.png" alt="UIPresenter example 1 screenshot" width="360" />
<img src="/screenshots/device-2021-12-15-175858.png" alt="UIPresenter example 2 screenshot" width="360" />
</div>

# How to get this project

**Step 1.** Add the jitpack repository to your ```project build.gradle``` file, like so:

```groovy
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    ext {
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
    }
}
// Place the jitpack repository inside this, like so:
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

**Step 2.** Add the dependency in your ``` module build.gradle ``` file, like so:

```groovy
dependencies {
    implementation 'com.github.germainkevinbusiness:UIPresente:1.0.0-beta04'
}
```

**That's it!**

## Usage

This library is only functional when called from a class that is either an Activity or a Fragment.

Basic usage is shown below, there's a more elaborate example in
the [sample app](https://github.com/germainkevinbusiness/UIPresenter/tree/master/sample). Inside
your Activity or Fragment, write this, where you see fit & safe to reference the View you want to
present:

```kotlin

UIPresenter(activity = this).set(
    viewToPresent = binding.recyclerView[2], // using view binding here
    descriptionText = "This is a row inside the RecyclerView with an animal image and name",
    presenterStateChangeListener = { _, _ -> }
)
```

### Here's the output of the above code in light and dark mode:

<div>
<img src="/screenshots/device-2021-12-15-181310.png" alt="Light mode UIPresenter example" width="360" />
<img src="/screenshots/device-2021-12-15-181210.png" alt="Dark mode UIPresenter example" width="360" />
</div>

Or if you just want to specify even more values such as text size, typeface,shadow layer, animation,
animation duration, listening to state changes:

```kotlin
private val teal200 = ContextCompat.getColor(this, R.color.teal200)
private val purple700 = ContextCompat.getColor(this, R.color.purple700)
private val whiteColor = ContextCompat.getColor(this, R.color.whiteColor)
private val descText0 =
    "This is the EditText. Here you can write Animal names and add them to the RecyclerView"

private fun presentEditText() {
    UIPresenter(this).set(
        viewToPresent = binding.addEditText,
        backgroundColor = teal200,
        descriptionTextColor = Color.BLACK,
        descriptionText = descText0,
        revealAnimation = CircularRevealAnimation(),
        presenterHasShadowedWindow = true,
        shadowLayer = PresenterShadowLayer(dx = 8f, dy = 8f, shadowColor = Color.DKGRAY),
        removePresenterOnAnyClickEvent = false,
        presenterStateChangeListener = { state, removePresenter ->
            // This condition says to remove the presenter when a click is done on the presenter
            // and to go to the presentMenuItem() function
            if (state == Presenter.STATE_FOCAL_PRESSED) {
                removePresenter()
                presentMenuItem()
            }
        }
    )
}

// Or for a Menu item in your toolbar
private fun presentMenuItem() {
    val descText0 = "This is a play button, placed on a Toolbar"
    UIPresenter(this).set(
        viewToPresentId = R.id.menu_item_play_btn,
        backgroundColor = purple700,
        descriptionText = descText0,
        descriptionTextColor = whiteColor,
        revealAnimation = RotationYByAnimation(),
        presenterHasShadowedWindow = true,
        removePresenterOnAnyClickEvent = false,
        shadowLayer = PresenterShadowLayer(shadowColor = blue500),
        presenterStateChangeListener = { state, removePresenter ->
            if (state == Presenter.STATE_FOCAL_PRESSED) {
                removePresenter()
                Toast.makeText(this, "Done presenting UI!", Toast.LENGTH_SHORT).show()
            }
        }
    )
}
```

### Here's the output function by function of the above code:

<div>
<img src="/screenshots/device-2021-12-15-175954.png" alt="An EditText being presented by the library" width="360" />
<img src="/screenshots/device-2021-12-15-182506.png" alt="A Menu item being presented by the library" width="360" />
</div>

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
        removePresenter()
    }
)
````

If you want to remove the Presenter on certain specific click events, here are the click events that
the UIPresenter library can detect:

```kotlin
Presenter.STATE_VTP_PRESSED, // When a click is done on the view you want to present
Presenter.STATE_FOCAL_PRESSED, // when a click is done on the presenter
Presenter.STATE_NON_FOCAL_PRESSED, // when a click is done outside the present and the view you want to present
Presenter.STATE_BACK_BUTTON_PRESSED // when the user presses the back button

UIPresenter(fragment = this).set(
    removePresenterOnAnyClickEvent = false,
    presenterStateChangeListener = { state, removePresenter ->
        // this says to remove the presenter when a click is done on it
        if (state == Presenter.STATE_FOCAL_PRESSED) {
            removePresenter()
        }
    }
)
```

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
