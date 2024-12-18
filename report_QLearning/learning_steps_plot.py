import pandas as pd
import matplotlib.pyplot as plt
from matplotlib.widgets import Slider
import re

# グローバル変数の定義
learning_counts = []
step_counts = []
ax = None
slider_epsilon = None
slider_alpha = None
slider_gamma = None

# グラフの更新関数
def update(val):
    global slider_epsilon, slider_alpha, slider_gamma  # グローバル変数を使用
    if slider_epsilon is not None and slider_alpha is not None and slider_gamma is not None:
        epsilon = slider_epsilon.val
        alpha = slider_alpha.val
        gamma = slider_gamma.val

        # グラフを更新
        ax.clear()
        ax.plot(learning_counts, step_counts, marker='o', color='blue', linestyle='-')
        ax.set_title(f'学習回数とゴールまでのステップ数の関係\nε={epsilon:.2f}, α={alpha:.2f}, γ={gamma:.2f}')
        ax.set_xlabel('学習回数')
        ax.set_ylabel('ゴールまでのステップ数')
        ax.grid()
        plt.draw()

# メイン関数
def main():
    global ax, slider_epsilon, slider_alpha, slider_gamma  # グローバル変数を使用

    # ユーザーにCSVファイルのパスを入力させる
    csv_file = input("CSVファイルのパスを入力してください: ")

    try:
        # CSVファイルを読み込む
        with open(csv_file, 'r', encoding='utf-8') as file:
            lines = file.readlines()

        # パラメータとデータの抽出
        current_params = None
        for line in lines:
            # パラメータの行を探す
            param_match = re.search(r'パラメータ\s+([\d.]+),([\d.]+),([\d.]+)', line)
            if param_match:
                current_params = (float(param_match.group(1)), float(param_match.group(2)), float(param_match.group(3)))
                continue
            
            # 学習回数とゴールまでのステップ数を抽出
            match = re.search(r'学習回数：(\d+)\s+ゴールまでのステップ数:(\d+)', line)
            if match and current_params is not None:
                learning_counts.append(int(match.group(1)))  # 学習回数
                step_counts.append(int(match.group(2)))       # ステップ数

        # グラフの作成
        fig, ax = plt.subplots(figsize=(10, 5))
        plt.subplots_adjust(bottom=0.25)

        # スライダーの位置とサイズ
        ax_slider_epsilon = plt.axes([0.1, 0.1, 0.65, 0.03])
        ax_slider_alpha = plt.axes([0.1, 0.15, 0.65, 0.03])
        ax_slider_gamma = plt.axes([0.1, 0.2, 0.65, 0.03])

        # スライダーの作成
        slider_epsilon = Slider(ax_slider_epsilon, 'ε', 0.0, 1.0, valinit=current_params[0], valstep=0.1)
        slider_alpha = Slider(ax_slider_alpha, 'α', 0.0, 1.0, valinit=current_params[1], valstep=0.1)
        slider_gamma = Slider(ax_slider_gamma, 'γ', 0.0, 1.0, valinit=current_params[2], valstep=0.1)

        # スライダーの更新イベント
        slider_epsilon.on_changed(update)
        slider_alpha.on_changed(update)
        slider_gamma.on_changed(update)

        # 初期グラフの描画
        update(0)

        # グラフを表示
        plt.show()

    except FileNotFoundError:
        print("指定されたファイルが見つかりません。")
    except Exception as e:
        print(f"エラーが発生しました: {e}")

if __name__ == "__main__":
    main()