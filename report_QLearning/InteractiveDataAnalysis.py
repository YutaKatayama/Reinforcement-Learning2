# パラメータを手動でスライドさせて学習回数を表示させる分析用プログラム
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.widgets import Slider

def main():
    # ユーザーにCSVファイルのパスを入力させる
    print("=== Q学習データのインタラクティブプロット ===")
    file_path = input("CSVファイルのパスを入力してください（例: output.csv）:\n")

    try:
        # CSVファイルを読み込む
        df = pd.read_csv(file_path, header=None, names=['データ'])
        
        # データを分割して新しいカラムを作成
        df[['ε', 'α', 'γ', '学習回数', 'ステップ数']] = df['データ'].str.extract(r'ε:(\d+\.\d+) α:(\d+\.\d+) γ:(\d+\.\d+) 学習回数:(\d+) ステップ数:(\d+)')
        
        # データ型を変換
        df['ε'] = df['ε'].astype(float)
        df['α'] = df['α'].astype(float)
        df['γ'] = df['γ'].astype(float)
        df['学習回数'] = pd.to_numeric(df['学習回数'], errors='coerce').fillna(0).astype(int)  # NaNを0に置き換え
        df['ステップ数'] = pd.to_numeric(df['ステップ数'], errors='coerce').fillna(0).astype(int)  # NaNを0に置き換え

        # NaNを含む行を削除
        df.dropna(inplace=True)

        # プロットの作成
        fig, ax = plt.subplots(figsize=(10, 6))
        plt.subplots_adjust(bottom=0.25, right=0.75)  # 右側にスペースを作る

        # 初期値を設定
        initial_epsilon = df['ε'].mean()
        initial_alpha = df['α'].mean()
        initial_gamma = df['γ'].mean()

        # 学習回数を取得
        learning_count = df.loc[
            (df['ε'] == initial_epsilon) & 
            (df['α'] == initial_alpha) & 
            (df['γ'] == initial_gamma), 
            '学習回数'
        ]

        # 学習回数が見つからない場合のデフォルト値
        if learning_count.empty:
            learning_count = 0
        else:
            learning_count = learning_count.values[0]

        # ステップ数を取得
        step_count = df.loc[
            (df['学習回数'] == learning_count), 
            'ステップ数'
        ].values[0] if not df.loc[(df['学習回数'] == learning_count), 'ステップ数'].empty else 0

        # 学習回数とステップ数を表示
        text_learning_count = ax.text(0.5, 0.9, f'学習回数: {learning_count}, ステップ数: {step_count}', fontsize=12, ha='center')

        # スライダーの設定
        ax_slider_epsilon = plt.axes([0.1, 0.1, 0.65, 0.03])
        ax_slider_alpha = plt.axes([0.1, 0.15, 0.65, 0.03])
        ax_slider_gamma = plt.axes([0.1, 0.2, 0.65, 0.03])

        # スライダーの範囲を0.1間隔に設定
        slider_epsilon = Slider(ax_slider_epsilon, 'ε', df['ε'].min(), df['ε'].max(), valinit=initial_epsilon, valstep=0.1)
        slider_alpha = Slider(ax_slider_alpha, 'α', df['α'].min(), df['α'].max(), valinit=initial_alpha, valstep=0.1)
        slider_gamma = Slider(ax_slider_gamma, 'γ', df['γ'].min(), df['γ'].max(), valinit=initial_gamma, valstep=0.1)

        # グラフの範囲を固定
        ax.set_xlim(df['学習回数'].min(), df['学習回数'].max())
        ax.set_ylim(df['ステップ数'].min(), df['ステップ数'].max())

        # パラメータ表示用のテキストボックスを作成
        param_ax = fig.add_axes([0.8, 0.1, 0.15, 0.8])  # 右側に新しい軸を追加
        param_ax.axis('off')  # 軸を非表示にする

        # スライダーの更新イベント
        def update(val):
            epsilon = slider_epsilon.val
            alpha = slider_alpha.val
            gamma = slider_gamma.val

            # 学習回数を取得
            learning_count = df.loc[
                (df['ε'] == epsilon) & 
                (df['α'] == alpha) & 
                (df['γ'] == gamma), 
                '学習回数'
            ]

            if not learning_count.empty:
                learning_count = learning_count.values[0]
                # ステップ数を取得
                step_count = df.loc[
                    (df['学習回数'] == learning_count), 
                    'ステップ数'
                ].values[0] if not df.loc[(df['学習回数'] == learning_count), 'ステップ数'].empty else 0
                text_learning_count.set_text(f'学習回数: {learning_count}, ステップ数: {step_count}')
            else:
                text_learning_count.set_text('学習回数: データなし, ステップ数: データなし')

            # グラフを更新
            ax.clear()
            ax.set_xlabel('学習回数')
            ax.set_ylabel('ステップ数')
            ax.set_xlim(df['学習回数'].min(), df['学習回数'].max())  # X軸の範囲を固定
            ax.set_ylim(df['ステップ数'].min(), df['ステップ数'].max())  # Y軸の範囲を固定
            ax.scatter(learning_count, step_count, color='blue')
            ax.set_title('学習回数とステップ数の関係')
            ax.grid()

            # パラメータを表示
            param_ax.clear()  # 以前のテキストをクリア
            param_ax.text(0.5, 0.9, f'ε: {epsilon:.2f}', fontsize=12, ha='center')
            param_ax.text(0.5, 0.7, f'α: {alpha:.2f}', fontsize=12, ha='center')
            param_ax.text(0.5, 0.5, f'γ: {gamma:.2f}', fontsize=12, ha='center')
            param_ax.text(0.5, 0.3, f'学習回数: {learning_count}', fontsize=12, ha='center')

            # プロットを再描画
            plt.draw()

        # スライダーの更新イベントを設定
        slider_epsilon.on_changed(update)
        slider_alpha.on_changed(update)
        slider_gamma.on_changed(update)

        # 初期プロットを表示
        ax.set_xlabel('学習回数')
        ax.set_ylabel('ステップ数')
        ax.set_xlim(df['学習回数'].min(), df['学習回数'].max())  # X軸の範囲を固定
        ax.set_ylim(df['ステップ数'].min(), df['ステップ数'].max())  # Y軸の範囲を固定
        ax.scatter(learning_count, step_count, color='blue')
        ax.set_title('学習回数とステップ数の関係')
        ax.grid()

        # パラメータを表示
        param_ax.text(0.5, 0.9, f'ε: {initial_epsilon:.2f}', fontsize=12, ha='center')
        param_ax.text(0.5, 0.7, f'α: {initial_alpha:.2f}', fontsize=12, ha='center')
        param_ax.text(0.5, 0.5, f'γ: {initial_gamma:.2f}', fontsize=12, ha='center')
        param_ax.text(0.5, 0.3, f'学習回数: {learning_count}', fontsize=12, ha='center')

        # プロットの表示
        plt.show()

    except FileNotFoundError:
        print(f"ファイルが見つかりません: {file_path}")
    except Exception as e:
        print(f"エラーが発生しました: {e}")

if __name__ == "__main__":
    main()