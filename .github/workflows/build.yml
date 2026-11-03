name: Build Plugin

on:
  push:
    branches:
      - master
      - main

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Build Plugins
        run: |
          chmod +x gradlew
          ./gradlew make makePluginsJson
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}

      - name: Commit built plugins to builds branch
        run: |
          git config --global user.name "GitHub Action"
          git config --global user.email "action@github.com"
          mkdir -p out_builds
          find . -name "*.cs3" -exec cp {} out_builds/ \;
          find . -name "plugins.json" -exec cp {} out_builds/ \;
          git checkout --orphan builds
          git rm -rf .
          cp -r out_builds/* . || true
          rm -rf out_builds
          git add .
          git commit -m "Build plugins" || true
          git push origin builds --force
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
