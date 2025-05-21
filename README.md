# Bottle Ship

> 本模组是 [Ship In A Bottle](https://modrinth.com/mod/vs-ship-in-a-bottle) 的 Forge 非官方重制版，请不要将本模组的问题反馈给 Ship in a
> Bottle 原作者。

> This mod is an unofficial remastered version of Forge from [Ship In A Bottle](https://modrinth.com/mod/vs-ship-in-a-bottle), please do not
> submit issues with this mod to Ship in a Bottle.

## 简介

- Bottle Ship 是一个基于 Forge 的 Minecraft 模组，允许你将 Valykrie Skies 的船只“收进瓶中”，并随时将其释放到世界的其他位置。该模组适用于需要移动或存储船只的场景。

- Bottle Ship is a Minecraft mod based on Forge that allows you to "bottle" ships from Valkyrie Skies and release them at any other location
  in the world. This mod is useful for scenarios where you need to move or store ships.

## 使用说明 / Usage

- 使用空瓶（Bottle Without Ship）长按右键船只，将其收进瓶中，获得“瓶中船”物品（Bottle With Ship）。
- 使用“瓶中船”物品可将船只释放到指定位置。
- 当船只收进瓶中时会被传送至远距离，释放时会回到玩家附近。

- Hold right-click on a ship with an empty bottle (Bottle Without Ship) to bottle the ship and obtain a "Bottle With Ship" item.
- Use the "Bottle With Ship" item to release the ship at a specified location.
- When a ship is bottled, it is teleported far away; when released, it returns near the player.

## 配置 / Configuration

- `chargeStrength`：释放船只时的最大距离。
- `chargeTime`：收集/释放船只所需蓄力时间（tick）。
- `cooldown`：物品冷却时间。

- `chargeStrength`: Maximum distance when releasing a ship.
- `chargeTime`: Charge time (in ticks) required to bottle/release a ship.
- `cooldown`: Item cooldown time.

## 常见问题

- **瓶中船物品丢失怎么办？**  
  使用以下命令找回你的船只：
- **What if I lose my Bottle With Ship item?**  
  Use the following commands to recover your ship:
  ```
  /vmod teleport <ShipName> <x> <y> <z> (0,0,0)
  ```
  ```
  /vs set-static <ShipName> false
  ```

## 注意事项 / Notes

- 本模组处于测试阶段，可能存在 bug。
- 请勿将本模组相关问题反馈至 Ship In A Bottle 原作者。

- This mod is in testing and may contain bugs.
- Please do not report issues related to this mod to the original Ship In A Bottle author.
