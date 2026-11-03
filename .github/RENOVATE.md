# Renovateの導入

公式の[Mend Renovate GitHub App](https://github.com/apps/renovate)を使用します。
個人のトークンや`RENOVATE_TOKEN` Secret、Renovate実行用のGitHub Actionsは不要です。

## 初回設定

1. `.github/renovate.json`をリポジトリの既定ブランチに反映します。
2. [Renovate App](https://github.com/apps/renovate)のInstallまたはConfigureから
   `Sumire-Labs/Celeritas-Extra`を対象リポジトリに追加します。
   このテンプレートから別のModを作った場合は、そのリポジトリを選んでください。
3. 初期設定用のConfigure Renovate PRが作成された場合は、設定を確認してマージします。
   有効な`.github/renovate.json`が既にある場合は、その設定が使用されます。

実際の更新PRが作られるのは、設定した時間帯に更新が見つかった場合です。
GitHub Appのアクセス許可はGitHub側で承認してください。
以前のAction方式用に`RENOVATE_TOKEN`を登録済みであれば、この構成では使用しません。

## 実行内容

- `Asia/Tokyo`の毎週月曜0:00〜6:00に、更新PRの作成・更新を許可します。
  ホスト型Appの巡回時刻に依存するため、特定の分での実行は保証されません。
  この設定はAppの検査自体を週1回にするものではなく、変更を提出する時間帯の指定です。
- 更新があれば全更新を`renovate-weekly/`の1つのPRにまとめます。
  未マージのPRは同じ時間帯に更新し、更新がなければ新しいPRは作りません。
- メジャー更新も対象です。自動マージはせず、Build CIを確認してからマージします。
- Gradleプラグイン、Maven依存、Gradle Wrapper、GitHub Actionsを追跡します。
  Renovate本体はApp運営側で更新されます。
  特殊な`cleanroom.userdev(...)`は`gradle.properties`の`cleanroom_version`を専用ルールで検出します。
- Javaの基準は25に固定し、setup-javaがその最新パッチを解決します。
  Cleanroomは0.7.xのalpha/beta/rc/正式版を対象とし、開発ビルドや0.8以降への移行は含めません。
- Mixin・MixinExtras・LWJGLはLoaderに追従します。
  Eclipse formatterはSpotlessのリリースが固定する推奨版に追従します。

- [Renovate Appの導入説明](https://docs.renovatebot.com/getting-started/installing-onboarding/)
- [スケジュール設定](https://docs.renovatebot.com/configuration-options/#schedule)
