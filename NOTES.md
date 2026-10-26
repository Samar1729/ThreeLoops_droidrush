# Manual verification notes

## R-3b MediaStore paging

On an Android device with image access granted, open Albums and confirm `Photos on device: N` displays the MediaStore image count. Tap **Load first 50** and filter Logcat by `MirrorMediaStore`. The output reports both page sizes, page IDs, `strictlyAscending`, and `noOverlap`. Both pages are requested with a limit of 50; page 2 uses page 1's final `_id` as `afterId`. Confirm both sizes are at most 50 and both flags are `true` (empty pages are valid when the device has fewer than 51 images).
