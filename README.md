# Reinforcement-Learning2
強化学習の課題用レポジトリ

### ブランチの説明
feature/make_csv
- csvファイルに保存する機能を実装。csvに保存する機能は、どこのブランチでも使用できるようにしてあります。このブランチに切り替えて実行する必要はないです。

feature/Automated-Hyperparamete-Tuning
- ハイパラメータを等間隔で自動変更し、実行する機能を実装。手動で実行する手間を省略できる。

feature/repeated-executions
- 同様のハイパラメータで、任意回数、繰り返し実行してくれる機能を実装。Q学習は学習回数の不安定性があるため、単純に一度実行して出てきた値を信用できない。
