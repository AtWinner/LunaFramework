# ToggleButton

# 导入

```
allprojects {
    repositories {
        jcenter()
        maven { url "https://github.com/AtWinner/LunaFramework/raw/master/" }//
    }
}
```

```
dependencies {
    compile 'com.luna:toggle_button:1.0.0'
}
```


# 使用

```
    <com.luna.togglebutton.ToggleButton
        android:layout_width="45dp"
        android:layout_height="25dp"
        android:layout_marginRight="15dp"
        toggle:offBorderColor="#d9d8d8"
        toggle:offColor="#d9d8d8"
        toggle:onColor="#3F51B5"
        toggle:spotColor="#ffffff"
        toggle:toggleBorderWidth="0.5dp" />
```

# 效果

![展示效果](toggleButton.png)


# License

```
Copyright 2017 Hu Fanglin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```